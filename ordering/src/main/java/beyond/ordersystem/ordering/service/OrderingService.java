package beyond.ordersystem.ordering.service;

import beyond.ordersystem.common.dto.CommonDto;
import beyond.ordersystem.common.service.SseAlarmService;
import beyond.ordersystem.ordering.domain.OrderDetail;
import beyond.ordersystem.ordering.domain.OrderStatus;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.dto.OrderCreateDto;
import beyond.ordersystem.ordering.dto.OrderDetailDto;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.ordering.dto.ProductDto;
import beyond.ordersystem.ordering.feignclient.ProductFeignClient;
import beyond.ordersystem.ordering.repository.OrderingDetailRepository;
import beyond.ordersystem.ordering.repository.OrderingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final OrderingDetailRepository orderingDetailRepository;
    private final SseAlarmService sseAlarmService;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 주문 생성
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long createOrdering(List<OrderCreateDto> dtos, String email) {

        // 주문 먼저 저장하고
        Ordering ordering = Ordering.builder().orderStatus(OrderStatus.ORDERED).memberEmail(email).build();
        orderingRepository.save(ordering);

        // 재고 조회
        for(OrderCreateDto dto : dtos) {
            // 상품 조회
            String productDetailUrl = "http://product-service/product/detail/" + dto.getProductId();
            HttpHeaders headers = new HttpHeaders();
            // HttpEntity: httpbody와 httpheader를 세팅하기 위한 객체
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            // 원래는 아래 요청의 예외를 try-catch 해줘야 함
            ResponseEntity<CommonDto> responseEntity = restTemplate.exchange(productDetailUrl, HttpMethod.GET, httpEntity, CommonDto.class);
            CommonDto commonDto = responseEntity.getBody();

            ObjectMapper objectMapper = new ObjectMapper();

            // readValue: String -> 클래스 변환, convertValue: Object 클래스 -> 클래스 변환
            ProductDto product = objectMapper.convertValue(commonDto.getResult(), ProductDto.class);

            int quantity = dto.getProductCount();

            // 재고 관리
//            if (product.getStockQuantity() < dto.getProductCount()) {
//                throw new IllegalArgumentException("재고가 부족합니다.");
//            }

            // 주문 발생
            OrderDetail orderDetail = OrderDetail.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(quantity)
                    .ordering(ordering)
                    .build();
            // @OneToMany + Cascade 조합으로 따로 save 없이 저장될 수 있게
            ordering.getOrderDetailList().add(orderDetail);

            // 동기적 재고 감소 요청
            String productUpdateStockUrl = "http://product-service/product/updatestock";
            HttpHeaders stockHeaders = new HttpHeaders();
            stockHeaders.setContentType(MediaType.APPLICATION_JSON);
            // HttpEntity: http body & http header를 세팅하기 위한 객체
            HttpEntity<OrderCreateDto> updateStockEntity = new HttpEntity<>(dto, stockHeaders);
            restTemplate.exchange(productUpdateStockUrl, HttpMethod.PUT, updateStockEntity, Void.class);
        }

        sseAlarmService.publishMessage("admin@naver.com", email, ordering.getId());

        return ordering.getId();
    }

    // Fall back 메서드는 원본 메서드의 매개변수와 정확히 일치해야 함.
    public void fallbackProductServiceCircuit(List<OrderCreateDto> dtos, String email, Throwable t) {
        throw new RuntimeException("상품 서버 응답 없음. 나중에 다시 시도해주세요." + t);
    }

    // 테스트: 4 ~ 5번의 정상 요청 -> 5번 중에 2번의 지연 발생 -> circuit open -> 그 다음 요청은 바로 fall back
    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "fallbackProductServiceCircuit")
    public Long createFeignKafka(List<OrderCreateDto> dtos, String email) {

        Ordering ordering = Ordering.builder().orderStatus(OrderStatus.ORDERED).memberEmail(email).build();
        orderingRepository.save(ordering);

        // 재고 조회
        for (OrderCreateDto dto : dtos) {
            // feign 클라이언트를 사용한 상품 조회
            CommonDto commonDto = productFeignClient.getProductById(dto.getProductId());

            ObjectMapper objectMapper = new ObjectMapper();
            ProductDto product = objectMapper.convertValue(commonDto.getResult(), ProductDto.class);

            int quantity = dto.getProductCount();

            // 주문 발생
            OrderDetail orderDetail = OrderDetail.builder().productId(product.getId()).productName(product.getName()).quantity(quantity).ordering(ordering).build();
            ordering.getOrderDetailList().add(orderDetail);

            // feign을 통한 동기적 재고 감소 요청
//            productFeignClient.updateProductStockQuantity(dto);

            // kafka를 활용한 비동기적 재고 감소
            kafkaTemplate.send("stock-update-topic", dto);
        }

        sseAlarmService.publishMessage("admin@naver.com", email, ordering.getId());

        return ordering.getId();
    }

    // 주문 목록 조회
    public List<OrderListResDto> getOrderingList() {
        List<OrderListResDto> orderListResDtoList = new ArrayList<>();
        List<Ordering> orderingList = orderingRepository.findAll();

        // Ordering (id, orderStatus), Member(memberEmail)
        // OrderDetail (detailId, productName, productCount)
        for (Ordering ordering : orderingList) {
            List<OrderDetailDto> orderDetailDtoList = new ArrayList<>();

            List<OrderDetail> orderDetailList = orderingDetailRepository.findByOrdering(ordering);
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetailDtoList.add(OrderDetailDto.fromEntity(orderDetail));
            }
            OrderListResDto orderListResDto = OrderListResDto.fromEntity(ordering, orderDetailDtoList);
            orderListResDtoList.add(orderListResDto);
        }

        return orderListResDtoList;
    }

    // 나의 주문 목록 조회
    public List<OrderListResDto> getMyOrderingList(String email) {

        List<Ordering> orderingList = orderingRepository.findByMemberEmail(email);

        List<OrderListResDto> orderListResDtoList = new ArrayList<>();

        for (Ordering ordering : orderingList) {
            List<OrderDetailDto> orderDetailDtoList = new ArrayList<>();

            List<OrderDetail> orderDetailList = orderingDetailRepository.findByOrdering(ordering);

            for(OrderDetail orderDetail : orderDetailList) {
                orderDetailDtoList.add(OrderDetailDto.fromEntity(orderDetail));
            }
            OrderListResDto orderListResDto = OrderListResDto.fromEntity(ordering, orderDetailDtoList);
            orderListResDtoList.add(orderListResDto);
        }

        return orderListResDtoList;
    }
}

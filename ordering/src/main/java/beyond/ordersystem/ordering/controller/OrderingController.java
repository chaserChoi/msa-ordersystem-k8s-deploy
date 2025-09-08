package beyond.ordersystem.ordering.controller;

import beyond.ordersystem.common.dto.CommonDto;
import beyond.ordersystem.ordering.domain.Ordering;
import beyond.ordersystem.ordering.dto.OrderCreateDto;
import beyond.ordersystem.ordering.dto.OrderListResDto;
import beyond.ordersystem.ordering.service.OrderingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ordering")
public class OrderingController {

    private final OrderingService orderingService;

    // 주문 생성
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody @Valid List<OrderCreateDto> dto, @RequestHeader("X-User-Email") String email) {

        Long id = orderingService.createFeignKafka(dto, email);

        return new ResponseEntity<>(
                new CommonDto(id, HttpStatus.OK.value(), "주문 성공"),
                HttpStatus.CREATED
        );
    }

    // 주문 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> getOrderingList() {

        List<OrderListResDto> orderListResDtoList = orderingService.getOrderingList();

        return new ResponseEntity<>(
                new CommonDto(orderListResDtoList, HttpStatus.OK.value(), "주문목록 조회 성공"),
                HttpStatus.OK
        );
    }

    // 나의 주문 목록 조회
    @GetMapping("/myorders")
    public ResponseEntity<?> getMyOrderingList(@RequestHeader("X-User-Email") String email) {

        List<OrderListResDto> orderListResDtoList = orderingService.getMyOrderingList(email);

        return new ResponseEntity<>(
                new CommonDto(orderListResDtoList, HttpStatus.OK.value(), "나의 주문목록 조회 성공"),
                HttpStatus.OK
        );
    }
}

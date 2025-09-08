package beyond.ordersystem.ordering.feignclient;

import beyond.ordersystem.common.dto.CommonDto;
import beyond.ordersystem.ordering.dto.OrderCreateDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

// name 부분은 eureka에 등록된 application.name을 의미
@FeignClient(name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/product/detail/{productId}")
    CommonDto getProductById(@PathVariable Long productId);

    @PutMapping("/product/updatestock")
    void updateProductStockQuantity(@RequestBody OrderCreateDto orderCreateDto);
}

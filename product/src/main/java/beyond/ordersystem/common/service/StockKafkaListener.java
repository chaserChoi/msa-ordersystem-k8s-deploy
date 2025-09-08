package beyond.ordersystem.common.service;

import beyond.ordersystem.product.dto.ProductUpdateStockDto;
import beyond.ordersystem.product.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockKafkaListener {

    private final ProductService productService;

    @KafkaListener(topics = "stock-update-topic", containerFactory = "kafkaListener") // containerFactory -> method명
    public void stockConsumer(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ProductUpdateStockDto dto = objectMapper.readValue(message, ProductUpdateStockDto.class);
            productService.updateStock(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

package beyond.ordersystem.ordering.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderCreateDto {

    @NotNull(message = "상품 id가 비어있습니다.")
    private Long productId;
    @NotNull(message = "상품 개수가 비어있습니다.")
    private int productCount;
}

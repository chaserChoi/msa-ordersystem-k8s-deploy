package beyond.ordersystem.common.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CommonDto {

    private Object result;
    private int statusCode;
    private String statusMessage;

}

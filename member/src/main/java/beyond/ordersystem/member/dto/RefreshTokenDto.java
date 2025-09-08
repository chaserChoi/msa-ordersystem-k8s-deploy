package beyond.ordersystem.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 1. /refresh-token?token=XXXXX -> URL parameter로 받는 경우 중요 정보가 URL에 노출될 수 있음
 * 2. 해당 DTO 방식을 이용할 경우 중요 정보를 노출하지 않을 수 있음.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RefreshTokenDto {

    private String refreshToken;
}

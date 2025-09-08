package beyond.ordersystem.member.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginReqDto {

    @NotEmpty(message = "email이 비었습니다.")
    private String email;
    @NotEmpty(message = "password가 비어있습니다.")
    @Size(min = 8, message = "password가 너무 짧습니다.")
    private String password;


}

package beyond.ordersystem.member.dto;

import beyond.ordersystem.member.domain.Member;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 회원 가입 DTO
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MemberCreateDto {

    @NotEmpty(message = "name은 필수 입력 항목입니다.")
    private String name;
    @NotEmpty(message = "email은 필수 입력 항목입니다.")
    private String email;
    @NotEmpty(message = "password는 필수 입력 항목입니다.")
    @Size(min = 8, message = "패스워드의 길이가 너무 짧습니다.")
    private String password;

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPassword)
                .build();
    }

    /*public MemberCreateDto fromEntity(Member entity) {
        return MemberCreateDto.builder()
                .name(entity.getName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .build();
    }*/
}

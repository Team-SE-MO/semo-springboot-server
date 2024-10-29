package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordUpdate {

    @NotBlank
    @Email
    private String email;

    //     TODO: 비밀번호 조건 검증 필요 (+ 리팩토링 정규식 추가 예정)
    @NotBlank
    private String newPassword;

}

package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailAuthVerify {

    @Email(message = "이메일 형식이 아닙니다. 다시 확인 해주세요.")
    @NotBlank(message = "이메일을 반드시 입력해주세요.")
    public String email;

    @NotBlank(message = "인증코드를 반드시 입력해주세요.")
    private String authCode;

}

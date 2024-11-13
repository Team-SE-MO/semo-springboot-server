package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePassword {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String newPassword;

}

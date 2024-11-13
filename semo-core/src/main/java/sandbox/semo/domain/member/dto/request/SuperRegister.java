package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SuperRegister {

    @NotNull
    private Long companyId;

    @NotBlank
    private String loginId;

    @NotBlank
    private String password;

    @NotBlank
    private String ownerName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String key;

}

package sandbox.semo.domain.form.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberFormRegister {

    @NotNull
    private Long companyId;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String ownerName;
}

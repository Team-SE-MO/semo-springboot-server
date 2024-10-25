package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRegister {

    @NotNull
    private Long companyId;

    @NotBlank
    private String ownerName;

    @NotBlank
    @Email
    private String email;


}

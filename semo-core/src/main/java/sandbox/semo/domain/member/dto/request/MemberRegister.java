package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRegister {

    @NotNull
    private Long companyId;

    @NotNull
    private String loginId;

    @NotNull
    private String ownerName;

    @NotNull
    private String password;

    @NotNull
    private String role;

}

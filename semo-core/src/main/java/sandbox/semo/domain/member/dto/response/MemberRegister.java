package sandbox.semo.domain.member.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRegister {

    @NotNull
    private String email;

    @NotNull
    private String loginId;

    @NotNull
    private String ownerName;

    @NotNull
    private String password;

}

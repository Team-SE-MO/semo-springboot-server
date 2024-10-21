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

    public MemberRegister(String email, String loginId, String ownerName, String password) {
        this.email = email;
        this.loginId = loginId;
        this.ownerName = ownerName;
        this.password = password;
    }
}

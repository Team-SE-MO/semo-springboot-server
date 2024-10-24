package sandbox.semo.domain.member.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberRegisterRejection {

    @NotNull
    private String email;

}

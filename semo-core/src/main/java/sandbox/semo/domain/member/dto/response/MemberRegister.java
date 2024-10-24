package sandbox.semo.domain.member.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

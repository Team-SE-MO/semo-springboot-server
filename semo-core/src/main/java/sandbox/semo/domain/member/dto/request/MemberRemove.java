package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import sandbox.semo.domain.member.entity.Role;

@Builder
@AllArgsConstructor
@Data
public class MemberRemove {

    @NotNull
    private Role role;

    @NotNull
    private Long companyId;

    @NotNull
    private String loginId;


}

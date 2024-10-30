package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import sandbox.semo.domain.member.entity.Role;

@Data
public class MemberSearchFilter {

    private Long companyId;
    
    @NotNull
    private List<Role> roleList;

    private String keyword;

}

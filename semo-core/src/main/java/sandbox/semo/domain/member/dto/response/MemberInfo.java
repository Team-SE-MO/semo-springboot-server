package sandbox.semo.domain.member.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.member.entity.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfo {

    private String loginId;
    private Role role;
    private String email;
    private String ownerName;

    private LocalDateTime deletedAt;

    private Company company;
    
}

package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberFormDecision {

    @NotNull
    private Long formId;

    @NotBlank
    private String decisionStatus;

}

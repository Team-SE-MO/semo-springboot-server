package sandbox.semo.domain.common.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import sandbox.semo.domain.common.entity.FormStatus;

@Data
@Builder
public class FormDecisionResponse {

    private FormStatus formStatus;
    private LocalDateTime approvedAt;

}

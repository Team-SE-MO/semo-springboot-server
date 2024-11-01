package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailSendRequest {

    @NotNull
    private String apiType;

    @NotNull
    private String value;

}

package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailRequest {

    @NotNull
    public String apiType;

    public String value;

    public String email;

    private String authCode;

}

package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailRegister {

    @NotNull
    private String email;

    @NotNull
    private String authCode;

    public String apiType;

    public String value;

    public EmailRegister(String value) {
    }
}

package sandbox.semo.domain.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailAuthVerify {

    @NotNull
    public String email;

    @NotNull
    private String authCode;

}

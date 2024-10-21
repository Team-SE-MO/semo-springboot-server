package sandbox.semo.member.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmailRegister {

    public EmailRegister() {
    }

    @NotNull
    private String email;

    public EmailRegister(String email) {
        this.email = email;
    }
}

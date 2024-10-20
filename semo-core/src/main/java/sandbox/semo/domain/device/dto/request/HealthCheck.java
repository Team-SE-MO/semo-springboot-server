package sandbox.semo.domain.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HealthCheck {

    @NotBlank
    private String type;

    @NotBlank
    private String ip;

    @NotBlank
    private Long port;

    @NotBlank
    private String sid;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

}

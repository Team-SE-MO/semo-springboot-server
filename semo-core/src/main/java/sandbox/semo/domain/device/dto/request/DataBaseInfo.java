package sandbox.semo.domain.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sandbox.semo.domain.device.entity.DatabaseType;

@Data
public class DataBaseInfo {

    @NotNull
    private DatabaseType type;

    @NotBlank
    private String ip;

    @NotNull
    private Long port;

    @NotBlank
    private String sid;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

}

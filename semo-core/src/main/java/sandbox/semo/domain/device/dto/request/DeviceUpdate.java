package sandbox.semo.domain.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceUpdate {

    @NotNull
    private String targetDevice;

    @NotBlank
    private String updateDeviceAlias;

    @NotNull
    private DataBaseInfo updateDeviceInfo;
}

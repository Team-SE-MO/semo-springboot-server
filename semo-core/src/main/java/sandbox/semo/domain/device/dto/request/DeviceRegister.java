package sandbox.semo.domain.device.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceRegister {

    @NotBlank
    private String deviceAlias;

    @Valid
    private DataBaseInfo dataBaseInfo;

}

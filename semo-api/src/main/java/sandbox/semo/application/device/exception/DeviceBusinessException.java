package sandbox.semo.application.device.exception;

import lombok.Getter;

@Getter
public class DeviceBusinessException extends RuntimeException {

    private final DeviceErrorCode deviceErrorCode;

    public DeviceBusinessException(DeviceErrorCode deviceErrorCode) {
        super(deviceErrorCode.getMessage());
        this.deviceErrorCode = deviceErrorCode;
    }
}

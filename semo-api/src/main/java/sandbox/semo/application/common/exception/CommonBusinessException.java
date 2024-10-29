package sandbox.semo.application.common.exception;

import lombok.Data;

@Data
public class CommonBusinessException extends RuntimeException {

    private final CommonErrorCode commonErrorCode;

    public CommonBusinessException(CommonErrorCode commonErrorCode) {
        super(commonErrorCode.getMessage());
        this.commonErrorCode = commonErrorCode;
    }
}

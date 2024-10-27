package sandbox.semo.application.common.exception;

import lombok.Data;

@Data
public class GlobalBusinessException extends RuntimeException {

    private final CommonErrorCode commonErrorCode;

    public GlobalBusinessException(CommonErrorCode commonErrorCode) {
        super(commonErrorCode.getMessage());
        this.commonErrorCode = commonErrorCode;
    }
}

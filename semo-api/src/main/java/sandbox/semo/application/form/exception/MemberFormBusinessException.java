package sandbox.semo.application.form.exception;

import lombok.Data;

@Data
public class MemberFormBusinessException extends RuntimeException {

    private final MemberFormErrorCode memberFormErrorCode;

    public MemberFormBusinessException(MemberFormErrorCode memberFormErrorCode) {
        super(memberFormErrorCode.getMessage());
        this.memberFormErrorCode = memberFormErrorCode;
    }
}

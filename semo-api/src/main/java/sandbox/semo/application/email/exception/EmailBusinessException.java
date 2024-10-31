package sandbox.semo.application.email.exception;

import lombok.Getter;

@Getter
public class EmailBusinessException extends RuntimeException {

    private final EmailErrorCode emailErrorCode;

    public EmailBusinessException(EmailErrorCode emailErrorCode) {
        super(emailErrorCode.getMessage());
        this.emailErrorCode = emailErrorCode;
    }
}

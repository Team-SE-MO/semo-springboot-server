package sandbox.semo.application.form.exception;

import lombok.Getter;

@Getter
public class CompanyFormBusinessException extends RuntimeException {

    private final CompanyFormErrorCode companyFormErrorCode;

    public CompanyFormBusinessException(CompanyFormErrorCode companyFormErrorCode) {
        super(companyFormErrorCode.getMessage());
        this.companyFormErrorCode = companyFormErrorCode;
    }
}

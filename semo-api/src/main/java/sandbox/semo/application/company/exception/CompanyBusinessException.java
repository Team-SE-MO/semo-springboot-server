package sandbox.semo.application.company.exception;

import lombok.Getter;

@Getter
public class CompanyBusinessException extends RuntimeException {

    private final CompanyErrorCode companyErrorCode;

    public CompanyBusinessException(CompanyErrorCode companyErrorCode) {
        super(companyErrorCode.getMessage());
        this.companyErrorCode = companyErrorCode;
    }
}


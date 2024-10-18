package sandbox.semo.application.form.service;

import static sandbox.semo.application.form.exception.CompanyFormErrorCode.COMPANY_ALREADY_EXISTS;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.form.exception.CompanyFormBusinessException;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.form.dto.request.CompanyFormRegister;
import sandbox.semo.domain.form.entity.CompanyForm;
import sandbox.semo.domain.form.entity.Status;
import sandbox.semo.domain.form.repository.CompanyFormRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyFormServiceImpl implements CompanyFormService {

    private final CompanyFormRepository companyFormRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public void companyRegister(CompanyFormRegister request) {
        checkCompanyExists(request.getTaxId());
        CompanyForm companyForm = CompanyForm.builder()
                .companyName(request.getCompanyName())
                .ownerName(request.getOwnerName())
                .taxId(request.getTaxId())
                .email(request.getEmail())
                .status(Status.PENDING)
                .build();

        companyFormRepository.save(companyForm);
    }

    private void checkCompanyExists(String taxId) {
        if (companyRepository.existsByTaxId(taxId)) {
            throw new CompanyFormBusinessException(COMPANY_ALREADY_EXISTS);
        }
    }

}

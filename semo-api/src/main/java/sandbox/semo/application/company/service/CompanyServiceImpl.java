package sandbox.semo.application.company.service;

import static sandbox.semo.application.company.exception.CompanyErrorCode.STATUS_NOT_APPROVED;
import static sandbox.semo.application.form.exception.CompanyFormErrorCode.FORM_DOES_NOT_EXIST;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.company.exception.CompanyBusinessException;
import sandbox.semo.application.form.exception.CompanyFormBusinessException;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.form.entity.CompanyForm;
import sandbox.semo.domain.form.entity.Status;
import sandbox.semo.domain.form.repository.CompanyFormRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyFormRepository companyFormRepository;
    private final CompanyRepository companyRepository;

    @Override
    @Transactional
    public Long companyRegister(Long formId) {
        CompanyForm companyForm = companyFormRepository.findById(formId)
                .orElseThrow(() -> new CompanyFormBusinessException(FORM_DOES_NOT_EXIST));

        if (!companyForm.getStatus().equals(Status.APPROVED)) {
            throw new CompanyBusinessException(STATUS_NOT_APPROVED);
        }

        Company company = Company.builder()
                .companyName(companyForm.getCompanyName())
                .taxId(companyForm.getTaxId())
                .build();

        companyRepository.save(company);
        return companyForm.getId();
    }
}

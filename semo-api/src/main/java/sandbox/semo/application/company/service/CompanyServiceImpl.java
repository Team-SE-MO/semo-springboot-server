package sandbox.semo.application.company.service;

import static sandbox.semo.application.company.exception.CompanyErrorCode.FORM_DOES_NOT_EXIST;
import static sandbox.semo.application.company.exception.CompanyErrorCode.STATUS_NOT_APPROVED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.company.exception.CompanyBusinessException;
import sandbox.semo.domain.common.entity.FormStatus;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.company.entity.CompanyForm;
import sandbox.semo.domain.company.repository.CompanyFormRepository;
import sandbox.semo.domain.company.repository.CompanyRepository;

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
                .orElseThrow(() -> new CompanyBusinessException(FORM_DOES_NOT_EXIST));

        if (!companyForm.getFormStatus().equals(FormStatus.APPROVED)) {
            throw new CompanyBusinessException(STATUS_NOT_APPROVED);
        }

        Company company = Company.builder()
                .companyName(companyForm.getCompanyName())
                .taxId(companyForm.getTaxId())
                .build();

        companyRepository.save(company);
        return companyForm.getId();
    }

    @Override
    public List<Company> searchCompanyByName(String keyword) {
        return companyRepository.findAllContainsKeywords(keyword);
    }
}

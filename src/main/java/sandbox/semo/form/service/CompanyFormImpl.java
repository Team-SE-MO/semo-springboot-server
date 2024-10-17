package sandbox.semo.form.service;

import static sandbox.semo.form.exception.CompanyFormErrorCode.COMPANY_ALREADY_EXISTS;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.company.repository.CompanyRepository;
import sandbox.semo.form.dto.CompanyFormRegister;
import sandbox.semo.form.entity.CompanyForm;
import sandbox.semo.form.entity.Status;
import sandbox.semo.form.exception.CompanyFormBusinessException;
import sandbox.semo.form.repository.CompanyFormRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyFormImpl implements CompanyFormService {

    private final CompanyFormRepository companyFormRepository;
    private final CompanyRepository companyRepository;

    @Override
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

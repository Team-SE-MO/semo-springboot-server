package sandbox.semo.application.company.service;

import static sandbox.semo.application.common.exception.CommonErrorCode.BAD_REQUEST;
import static sandbox.semo.application.company.exception.CompanyErrorCode.COMPANY_ALREADY_EXISTS;
import static sandbox.semo.application.company.exception.CompanyErrorCode.FORM_DOES_NOT_EXIST;
import static sandbox.semo.application.company.exception.CompanyErrorCode.STATUS_NOT_APPROVED;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.common.exception.CommonBusinessException;
import sandbox.semo.application.company.exception.CompanyBusinessException;
import sandbox.semo.domain.common.dto.response.FormDecisionResponse;
import sandbox.semo.domain.common.dto.response.OffsetPage;
import sandbox.semo.domain.common.entity.FormStatus;
import sandbox.semo.domain.company.dto.request.CompanyFormDecision;
import sandbox.semo.domain.company.dto.request.CompanyFormRegister;
import sandbox.semo.domain.company.dto.response.CompanyFormInfo;
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


    @Transactional
    @Override
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


    @Transactional
    @Override
    public void formRegister(CompanyFormRegister request) {
        checkCompanyExists(request.getTaxId());
        CompanyForm companyForm = CompanyForm.builder()
                .companyName(request.getCompanyName())
                .ownerName(request.getOwnerName())
                .taxId(request.getTaxId())
                .email(request.getEmail())
                .formStatus(FormStatus.PENDING)
                .build();

        companyFormRepository.save(companyForm);
    }

    private void checkCompanyExists(String taxId) {
        if (companyRepository.existsByTaxId(taxId)) {
            throw new CompanyBusinessException(COMPANY_ALREADY_EXISTS);
        }
    }

    @Override
    public OffsetPage<CompanyFormInfo> findForms(int page, int size) {
        if (page < 1) {
            throw new CommonBusinessException(BAD_REQUEST);
        }

        int offset = (page - 1) * size;
        List<CompanyForm> companyForms = companyFormRepository.findPageWithOffset(offset, size);
        long totalCount = companyFormRepository.count();
        List<CompanyFormInfo> content = companyForms.stream()
                .map(this::mapToCompanyFormInfo)
                .toList();
        int pageCount = (int) Math.ceil((double) totalCount / size);
        boolean hasNext = page < pageCount;
        return new OffsetPage<>(pageCount, content, hasNext);
    }

    private CompanyFormInfo mapToCompanyFormInfo(CompanyForm companyForm) {
        return CompanyFormInfo.builder()
                .formId(companyForm.getId())
                .companyName(companyForm.getCompanyName())
                .ownerName(companyForm.getOwnerName())
                .taxId(companyForm.getTaxId())
                .email(companyForm.getEmail())
                .formStatus(companyForm.getFormStatus())
                .requestDate(companyForm.getRequestDate())
                .approvedAt(companyForm.getApprovedAt())
                .build();
    }

    @Transactional
    @Override
    public FormDecisionResponse updateStatus(CompanyFormDecision request) {
        CompanyForm companyForm = companyFormRepository.findById(request.getFormId())
                .orElseThrow(() -> new CompanyBusinessException(FORM_DOES_NOT_EXIST));

        FormStatus newFormStatus = FormStatus.valueOf(request.getDecisionStatus().toUpperCase());
        companyForm.changeStatus(newFormStatus);
        return FormDecisionResponse.builder()
                .formStatus(companyForm.getFormStatus())
                .approvedAt(companyForm.getApprovedAt())
                .build();

    }

    @Override
    public Optional<Company> findByCompanyNameAndTaxId(String companyName, String taxId) {
        return companyRepository.findByCompanyNameAndTaxId(companyName, taxId);
    }

}

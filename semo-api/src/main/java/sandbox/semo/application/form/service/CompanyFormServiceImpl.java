package sandbox.semo.application.form.service;

import static sandbox.semo.application.form.exception.CompanyFormErrorCode.COMPANY_ALREADY_EXISTS;
import static sandbox.semo.application.form.exception.CompanyFormErrorCode.FORM_DOES_NOT_EXIST;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.form.exception.CompanyFormBusinessException;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.form.dto.request.CompanyFormDecision;
import sandbox.semo.domain.form.dto.request.CompanyFormRegister;
import sandbox.semo.domain.form.dto.response.CompanyFormList;
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
    public void formRegister(CompanyFormRegister request) {
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

    /*
     * TO DO : 0번째 에지부터 data가 없으면, 빈배열
     * totalPage를 넘어갔을 때 data가 없으면 예외처리 발생
     */
    @Override
    public Page<CompanyFormList> findAllForms(int page, int size) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("requestDate"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(sorts));
        Page<CompanyForm> companyFormPage = companyFormRepository.findAll(pageable);

        return companyFormPage.map(companyForm -> CompanyFormList.builder()
                .formId(companyForm.getId())
                .companyName(companyForm.getCompanyName())
                .ownerName(companyForm.getOwnerName())
                .taxId(companyForm.getTaxId())
                .email(companyForm.getEmail())
                .status(companyForm.getStatus())
                .requestDate(companyForm.getRequestDate())
                .approvedAt(companyForm.getApprovedAt())
                .build());
    }

    @Override
    @Transactional
    public String updateStatus(CompanyFormDecision request) {
        CompanyForm companyForm = companyFormRepository.findById(request.getFormId())
                .orElseThrow(() -> new CompanyFormBusinessException(FORM_DOES_NOT_EXIST));

        Status newStatus = Status.valueOf(request.getDecisionStatus().toUpperCase());
        companyForm.changeStatus(newStatus);
        return companyFormRepository.save(companyForm).getStatus().toString();

    }
}

package sandbox.semo.application.form.service;

import static sandbox.semo.application.form.exception.MemberFormErrorCode.COMPANY_NOT_EXIST;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.form.exception.MemberFormBusinessException;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.form.dto.request.MemberFormRegister;
import sandbox.semo.domain.form.entity.MemberForm;
import sandbox.semo.domain.form.entity.Status;
import sandbox.semo.domain.form.repository.MemberFormRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberFormServiceImpl implements MemberFormService {

    private final MemberFormRepository memberFormRepository;
    private final CompanyRepository companyRepository;
    
    @Override
    @Transactional
    public void formRegister(MemberFormRegister request) {
        Company requestCompany = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new MemberFormBusinessException(COMPANY_NOT_EXIST));

        MemberForm memberForm = MemberForm.builder()
                .companyName(requestCompany.getCompanyName())
                .email(request.getEmail())
                .ownerName(request.getOwnerName())
                .status(Status.PENDING)
                .build();

        memberFormRepository.save(memberForm);
    }

}

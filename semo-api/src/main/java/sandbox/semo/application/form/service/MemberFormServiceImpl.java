package sandbox.semo.application.form.service;

import static sandbox.semo.application.form.exception.MemberFormErrorCode.COMPANY_NOT_EXIST;
import static sandbox.semo.application.form.exception.MemberFormErrorCode.FORM_DOES_NOT_EXIST;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.form.exception.MemberFormBusinessException;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.form.dto.request.MemberFormDecision;
import sandbox.semo.domain.form.dto.request.MemberFormRegister;
import sandbox.semo.domain.form.dto.response.MemberFormList;
import sandbox.semo.domain.form.entity.MemberForm;
import sandbox.semo.domain.form.entity.Status;
import sandbox.semo.domain.form.repository.MemberFormRepository;

@Service
@Log4j2
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

        memberFormRepository.save(MemberForm.builder()
                .companyName(requestCompany.getCompanyName())
                .email(request.getEmail())
                .ownerName(request.getOwnerName())
                .status(Status.PENDING)
                .build());
        log.info(">>> [ ✅ 고객사 회원가입 폼이 성공적으로 등록되었습니다. ]");
    }

    /*
     * TO DO : 0번째 에지부터 data가 없으면, 빈배열
     * totalPage를 넘어갔을 때 data가 없으면 예외처리 발생
     */
    @Override
    public Page<MemberFormList> findAllForms(int page, int size) {
        List<Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("requestDate"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(sorts));
        Page<MemberForm> memberFormPage = memberFormRepository.findAll(pageable);

        return memberFormPage.map(memberForm -> MemberFormList.builder()
                .formId(memberForm.getId())
                .companyName(memberForm.getCompanyName())
                .ownerName(memberForm.getOwnerName())
                .email(memberForm.getEmail())
                .status(memberForm.getStatus())
                .requestDate(memberForm.getRequestDate())
                .approvedAt(memberForm.getApprovedAt())
                .build());
    }


    @Override
    @Transactional
    public String updateForm(MemberFormDecision request) {
        MemberForm memberForm = memberFormRepository.findById(request.getFormId())
                .orElseThrow(() -> new MemberFormBusinessException(FORM_DOES_NOT_EXIST));

        Status newStatus = Status.valueOf(request.getDecisionStatus().toUpperCase());
        memberForm.changeStatus(newStatus);
        MemberForm saveForm = memberFormRepository.save(memberForm);
        log.info(">>> [ ✅ 고객사 회원가입 폼을 관리자가 최종 처리하였습니다. ]");
        return saveForm.getStatus().toString();
    }

}

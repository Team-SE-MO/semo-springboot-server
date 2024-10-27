package sandbox.semo.application.member.service;

import static sandbox.semo.application.member.exception.MemberErrorCode.ALREADY_EXISTS_EMAIL;
import static sandbox.semo.application.member.exception.MemberErrorCode.COMPANY_NOT_EXIST;
import static sandbox.semo.application.member.exception.MemberErrorCode.FORM_DOES_NOT_EXIST;
import static sandbox.semo.application.member.exception.MemberErrorCode.INVALID_COMPANY_SELECTION;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.common.util.LoginIdGeneratorUtil;
import sandbox.semo.application.member.exception.MemberBusinessException;
import sandbox.semo.domain.common.entity.FormStatus;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.member.dto.response.MemberFormInfo;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.member.entity.MemberForm;
import sandbox.semo.domain.member.entity.Role;
import sandbox.semo.domain.member.repository.MemberFormRepository;
import sandbox.semo.domain.member.repository.MemberRepository;

// TODO: Security 설정 확인 용도로 가볍게 구현 했음. Member API 개발시 추가 코드 필요.
@Service
@Log4j2
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberFormRepository memberFormRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginIdGeneratorUtil loginIdGeneratorUtil;

    private static final String DEFAULT_PASSWORD = "0000";


    @Override
    @Transactional
    public String register(MemberRegister request, Role role) {
        checkEmailDuplicate(request.getEmail());
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new MemberBusinessException(COMPANY_NOT_EXIST));

        boolean isSuperRole = role.equals(Role.SUPER);

        Member member = Member.builder()
                .company(getCompanyById(request.getCompanyId()))
                .ownerName(request.getOwnerName())
                .loginId(generateLoginId(isSuperRole, company))
                .email(request.getEmail())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(determineRole(isSuperRole))
                .build();

        memberRepository.save(member);
        log.info(">>> [ ✅ 회원가입이 성공적으로 이루어졌습니다. ]");

        return member.getLoginId();
    }

    private String generateLoginId(boolean isSuperRole, Company company) {
        String rolePrefix = isSuperRole ? Role.ADMIN.toString() : Role.USER.toString();
        return loginIdGeneratorUtil.generateLoginId(rolePrefix, company.getTaxId());
    }

    private Role determineRole(boolean isSuperRole) {
        return isSuperRole ? Role.ADMIN : Role.USER;
    }


    @Transactional
    @Override
    public void formRegister(MemberFormRegister request) {
        checkEmailDuplicate(request.getEmail());
        if (request.getCompanyId() == 1L) {
            throw new MemberBusinessException(INVALID_COMPANY_SELECTION);
        }

        Company requestCompany = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new MemberBusinessException(COMPANY_NOT_EXIST));

        memberFormRepository.save(MemberForm.builder()
                .company(requestCompany)
                .email(request.getEmail())
                .ownerName(request.getOwnerName())
                .formStatus(FormStatus.PENDING)
                .build());
        log.info(">>> [ ✅ 고객사 회원가입 폼이 성공적으로 등록되었습니다. ]");
    }

    /**
     * TODO: 0번째 에지부터 data가 없으면, 빈배열
     * totalPage를 넘어갔을 때 data가 없으면 예외처리 발생
     **/
    @Override
    public Page<MemberFormInfo> findAllForms(int page, int size) {
        List<Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("requestDate"));

        Pageable pageable = PageRequest.of(page, size, Sort.by(sorts));
        Page<MemberForm> memberFormPage = memberFormRepository.findAll(pageable);

        return memberFormPage.map(memberForm -> MemberFormInfo.builder()
                .formId(memberForm.getId())
                .company(memberForm.getCompany())
                .ownerName(memberForm.getOwnerName())
                .email(memberForm.getEmail())
                .formStatus(memberForm.getFormStatus())
                .requestDate(memberForm.getRequestDate())
                .approvedAt(memberForm.getApprovedAt())
                .build());
    }


    @Override
    @Transactional
    public String updateForm(MemberFormDecision request) {
        MemberForm memberForm = memberFormRepository.findById(request.getFormId())
                .orElseThrow(() -> new MemberBusinessException(FORM_DOES_NOT_EXIST));
        FormStatus newFormStatus = FormStatus.valueOf(request.getDecisionStatus().toUpperCase());
        memberForm.changeStatus(newFormStatus);
        MemberForm saveForm = memberFormRepository.save(memberForm);
        log.info(">>> [ ✅ 고객사 회원가입 폼을 관리자가 최종 처리하였습니다. ]");
        return saveForm.getFormStatus().toString();
    }

    @Override
    public Boolean checkEmailDuplicate(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberBusinessException(ALREADY_EXISTS_EMAIL);
        }
        return true;
    }


}

package sandbox.semo.application.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.member.entity.Role;
import sandbox.semo.domain.member.repository.MemberRepository;

// TODO: Security 설정 확인 용도로 가볍게 구현 했음. Member API 개발시 추가 코드 필요.
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(MemberRegister request) {
        Member member = Member.builder()
                .company(getCompanyById(request.getCompanyId()))
                .ownerName(request.getOwnerName())
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .build();
        memberRepository.save(member);
    }

    private Company getCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }


    @Transactional
    @Override
    public void formRegister(MemberFormRegister request) {
        if (request.getCompanyId() == 1L) {
            throw new MemberBusinessException(INVALID_COMPANY_SELECTION);
        }

        Company requestCompany = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new MemberBusinessException(COMPANY_NOT_EXIST));

        memberFormRepository.save(MemberForm.builder()
                .companyName(requestCompany.getCompanyName())
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
                .companyName(memberForm.getCompanyName())
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


}

package sandbox.semo.application.member.service;

import static sandbox.semo.application.common.exception.CommonErrorCode.FORBIDDEN_ACCESS;
import static sandbox.semo.application.member.exception.MemberErrorCode.ALREADY_EXISTS_EMAIL;
import static sandbox.semo.application.member.exception.MemberErrorCode.COMPANY_NOT_EXIST;
import static sandbox.semo.application.member.exception.MemberErrorCode.FORM_DOES_NOT_EXIST;
import static sandbox.semo.application.member.exception.MemberErrorCode.INVALID_COMPANY_SELECTION;
import static sandbox.semo.application.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import sandbox.semo.application.common.exception.CommonBusinessException;
import sandbox.semo.application.member.exception.MemberBusinessException;
import sandbox.semo.application.member.service.helper.LoginIdGenerator;
import sandbox.semo.domain.common.entity.FormStatus;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.member.dto.request.MemberRemove;
import sandbox.semo.domain.member.dto.request.MemberSearchFilter;
import sandbox.semo.domain.member.dto.response.MemberFormInfo;
import sandbox.semo.domain.member.dto.response.MemberInfo;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.member.entity.MemberForm;
import sandbox.semo.domain.member.entity.Role;
import sandbox.semo.domain.member.repository.MemberFormRepository;
import sandbox.semo.domain.member.repository.MemberRepository;


@Service
@Log4j2
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberFormRepository memberFormRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginIdGenerator loginIdGenerator;

    private static final String DEFAULT_PASSWORD = "0000";


    private boolean isSuperRole(Role role) {
        return role.equals(Role.ROLE_SUPER);
    }


    @Override
    @Transactional
    public String register(MemberRegister request, Role role) {
        checkEmailDuplicate(request.getEmail());
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new MemberBusinessException(COMPANY_NOT_EXIST));

        Member member = Member.builder()
                .company(company)
                .ownerName(request.getOwnerName())
                .loginId(generateLoginId(isSuperRole(role), company))
                .email(request.getEmail())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(determineRole(isSuperRole(role)))
                .build();

        memberRepository.save(member);
        log.info(">>> [ ✅ 회원가입이 성공적으로 이루어졌습니다. ]");

        return member.getLoginId();
    }

    private String generateLoginId(boolean isSuperRole, Company company) {
        String rolePrefix = isSuperRole ? Role.ROLE_ADMIN.toString() : Role.ROLE_USER.toString();
        return loginIdGenerator.generateLoginId(rolePrefix, company.getTaxId());
    }

    private Role determineRole(boolean isSuperRole) {
        return isSuperRole ? Role.ROLE_ADMIN : Role.ROLE_USER;
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


    //TODO : DENIED 일때 approved_at 담기는 경우 리팩토링 예정
    @Override
    @Transactional
    public String updateForm(MemberFormDecision request) {
        MemberForm memberForm = memberFormRepository.findById(request.getFormId())
                .orElseThrow(() -> new MemberBusinessException(FORM_DOES_NOT_EXIST));
        FormStatus newFormStatus = FormStatus.valueOf(request.getDecisionStatus().toUpperCase());
        memberForm.changeStatus(newFormStatus);

        log.info(">>> [ ✅ 고객사 회원가입 폼을 관리자가 최종 처리하였습니다. ]");
        return newFormStatus.toString();
    }

    @Override
    public Boolean checkEmailDuplicate(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new MemberBusinessException(ALREADY_EXISTS_EMAIL);
        }
        return true;
    }


    @Transactional
    @Override
    public void updatePassword(Long memberId, String newPassword) {
        //     TODO: 비밀번호 조건 검증 필요 (+ 리팩토링 정규식 추가 예정)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberBusinessException(MEMBER_NOT_FOUND));

        member.changePassword(passwordEncoder.encode(newPassword));
        log.info(">>> [ ✅ 비밀번호 수정이 완료되었습니다. ]");
    }

    @Transactional
    @Override
    public void deleteMember(MemberRemove request) {
        Member member = memberRepository.findByLoginIdAndDeletedAtIsNull(request.getLoginId())
                .orElseThrow(() -> new MemberBusinessException(MEMBER_NOT_FOUND));

        validateDeletePermission(request, member);

        member.markAsDeleted();
        memberRepository.save(member);
    }

    private void validateDeletePermission(MemberRemove request, Member targetMember) {
        Role targetRole = targetMember.getRole();
        Role requestRole = request.getRole();

        boolean isTargetSuper = targetRole.equals(Role.ROLE_SUPER);
        // 본인 권한보다 아래의 권한인지 , 아니라면 예외
        if (isSuperRole(requestRole) && isTargetSuper) {
            log.warn(">>> [ ❌ SUPER는 자기자신을 삭제할 수 없습니다. ]");
            throw new CommonBusinessException(FORBIDDEN_ACCESS);
        }

        //ADMIN이면서, USER 외의 권한을 삭제하려 했을 때
        boolean isTargetUser = targetRole.equals(Role.ROLE_USER);
        if (!isSuperRole(requestRole) && !isTargetUser) {
            log.warn(">>> [ ❌ ADMIN은  USER외에는 삭제할 수 없습니다. ]");
            throw new CommonBusinessException(FORBIDDEN_ACCESS);
        }

        //ADMIN이 삭제할 USER랑 둘이 같은 회사인지 판별
        Long targetCompanyId = targetMember.getCompany().getId();
        if (!isSuperRole(requestRole) && (!request.getCompanyId().equals(targetCompanyId))) {
            log.warn(">>> [ ❌ ADMIN은  같은 회사만의 USER만 삭제할 수 있습니다. ]");
            throw new CommonBusinessException(FORBIDDEN_ACCESS);
        }

    }


    @Override
    public List<MemberInfo> findAllMembers(Long ownCompanyId, Role ownRole,
            MemberSearchFilter request) {
        boolean isSuperRole = isSuperRole(ownRole);
        boolean isOwnCompany = ownCompanyId.equals(request.getCompanyId());

        if (!isSuperRole && !isOwnCompany) {
            log.warn(">>> [ ❌ ADMIN이 다른 회사 유저목록을 조회할 수 없습니다. ]");
            throw new CommonBusinessException(FORBIDDEN_ACCESS);
        }

        List<Role> filterRoles = Optional.ofNullable(request.getRoleList())
                .filter(list -> !list.isEmpty())
                .orElse(List.of(Role.ROLE_USER, Role.ROLE_ADMIN));

        return memberRepository.findAllMemberContainsRole(request.getCompanyId(),
                request.getKeyword(),
                filterRoles);
    }


}

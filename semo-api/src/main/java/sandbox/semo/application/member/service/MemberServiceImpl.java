package sandbox.semo.application.member.service;

import static sandbox.semo.application.common.exception.CommonErrorCode.BAD_REQUEST;
import static sandbox.semo.application.common.exception.CommonErrorCode.FORBIDDEN_ACCESS;
import static sandbox.semo.application.member.exception.MemberErrorCode.ALREADY_EXISTS_EMAIL;
import static sandbox.semo.application.member.exception.MemberErrorCode.COMPANY_NOT_EXIST;
import static sandbox.semo.application.member.exception.MemberErrorCode.FORM_DOES_NOT_EXIST;
import static sandbox.semo.application.member.exception.MemberErrorCode.INVALID_COMPANY_SELECTION;
import static sandbox.semo.application.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;
import static sandbox.semo.application.member.exception.MemberErrorCode.UNAUTHORIZED_TO_MEMBER;
import static sandbox.semo.domain.member.entity.Role.ROLE_SUPER;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sandbox.semo.application.common.exception.CommonBusinessException;
import sandbox.semo.application.member.exception.MemberBusinessException;
import sandbox.semo.application.member.exception.MemberErrorCode;
import sandbox.semo.application.member.service.helper.LoginIdGenerator;
import sandbox.semo.domain.common.dto.response.OffsetPage;
import sandbox.semo.domain.common.dto.response.FormDecisionResponse;
import sandbox.semo.domain.common.entity.FormStatus;
import sandbox.semo.domain.company.entity.Company;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.member.dto.request.MemberRemove;
import sandbox.semo.domain.member.dto.request.MemberSearchFilter;
import sandbox.semo.domain.member.dto.request.SuperRegister;
import sandbox.semo.domain.member.dto.request.UpdatePassword;
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
    private static final String SUPER_KEY = "0000";

    @Override
    @Transactional
    public void superRegister(SuperRegister request) {
        if (!request.getKey().equals(SUPER_KEY)){
            throw new MemberBusinessException(UNAUTHORIZED_TO_MEMBER);
        }
        checkEmailDuplicate(request.getEmail());
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new MemberBusinessException(COMPANY_NOT_EXIST));
        Member member = Member.builder()
                .company(company)
                .ownerName(request.getOwnerName())
                .loginId(request.getLoginId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(ROLE_SUPER)
                .build();
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public String register(MemberRegister request, Role role) {
        checkEmailDuplicate(request.getEmail());
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new MemberBusinessException(COMPANY_NOT_EXIST));
        boolean isCheckRole = isSuperRole(role);

        Member member = Member.builder()
                .company(company)
                .ownerName(request.getOwnerName())
                .loginId(generateLoginId(isCheckRole, company))
                .email(request.getEmail())
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(determineRole(isCheckRole))
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

    @Override
    public OffsetPage<MemberFormInfo> findForms(int page, int size) {
        if (page < 1) {
            throw new CommonBusinessException(BAD_REQUEST);
        }

        int offset = (page - 1) * size;
        List<MemberForm> memberForms = memberFormRepository.findPageWithOffset(offset, size);
        long totalCount = memberFormRepository.count();
        List<MemberFormInfo> content = memberForms.stream()
                .map(this::mapToMemberFormInfo)
                .toList();
        int pageCount = (int) Math.ceil((double) totalCount / size);
        boolean hasNext = page < pageCount;
        return new OffsetPage<>(pageCount, content, hasNext);
    }

    private MemberFormInfo mapToMemberFormInfo(MemberForm memberForm) {
        return MemberFormInfo.builder()
                .formId(memberForm.getId())
                .company(memberForm.getCompany())
                .ownerName(memberForm.getOwnerName())
                .email(memberForm.getEmail())
                .formStatus(memberForm.getFormStatus())
                .requestDate(memberForm.getRequestDate())
                .approvedAt(memberForm.getApprovedAt())
                .build();
    }

    @Override
    @Transactional
    public FormDecisionResponse updateForm(MemberFormDecision request) {
        MemberForm memberForm = memberFormRepository.findById(request.getFormId())
                .orElseThrow(() -> new MemberBusinessException(FORM_DOES_NOT_EXIST));
        FormStatus newFormStatus = FormStatus.valueOf(request.getDecisionStatus().toUpperCase());
        memberForm.changeStatus(newFormStatus);

        log.info(">>> [ ✅ 고객사 회원가입 폼을 관리자가 최종 처리하였습니다. ]");
        return FormDecisionResponse.builder()
                .formStatus(memberForm.getFormStatus())
                .approvedAt(memberForm.getApprovedAt()).build();
    }

    @Override
    public Boolean checkEmailDuplicate(String email) {
        if (memberRepository.findByEmailAndDeletedAtIsNull(email).isPresent()) {
            throw new MemberBusinessException(ALREADY_EXISTS_EMAIL);
        }
        if (memberRepository.findByEmail(email).isPresent()) {
            throw new MemberBusinessException(MemberErrorCode.DELETED_MEMBER_EMAIL);
        }
        return true;
    }

    @Transactional
    @Override
    public void updatePassword(UpdatePassword request) {
        String email = request.getEmail();
        String newPassword = request.getNewPassword();
        Member member = memberRepository.findByEmailAndDeletedAtIsNull(email)
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
        boolean isCheckRole = isSuperRole(requestRole);

        boolean isTargetSuper = targetRole.equals(ROLE_SUPER);
        // 본인 권한보다 아래의 권한인지 , 아니라면 예외
        if (isCheckRole && isTargetSuper) {
            log.warn(">>> [ ❌ SUPER는 자기자신을 삭제할 수 없습니다. ]");
            throw new CommonBusinessException(FORBIDDEN_ACCESS);
        }

        //ADMIN이면서, USER 외의 권한을 삭제하려 했을 때
        boolean isTargetUser = targetRole.equals(Role.ROLE_USER);
        if (!isCheckRole && !isTargetUser) {
            log.warn(">>> [ ❌ ADMIN은  USER외에는 삭제할 수 없습니다. ]");
            throw new CommonBusinessException(FORBIDDEN_ACCESS);
        }

        //ADMIN이 삭제할 USER랑 둘이 같은 회사인지 판별
        Long targetCompanyId = targetMember.getCompany().getId();
        if (!isCheckRole && (!request.getCompanyId().equals(targetCompanyId))) {
            log.warn(">>> [ ❌ ADMIN은  같은 회사만의 USER만 삭제할 수 있습니다. ]");
            throw new CommonBusinessException(FORBIDDEN_ACCESS);
        }

    }

    private boolean isSuperRole(Role role) {
        return role.equals(ROLE_SUPER);
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

        return memberRepository.findAllActiveMemberContainsRole(request.getCompanyId(),
                request.getKeyword(),
                filterRoles);
    }

}

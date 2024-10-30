package sandbox.semo.application.member.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.member.service.MemberService;
import sandbox.semo.application.security.authentication.MemberPrincipalDetails;
import sandbox.semo.domain.company.repository.CompanyRepository;
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.member.dto.request.MemberRemove;
import sandbox.semo.domain.member.dto.request.MemberSearchFilter;
import sandbox.semo.domain.member.dto.response.MemberFormInfo;
import sandbox.semo.domain.member.dto.response.MemberInfo;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.member.entity.Role;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
@Validated
public class MemberController {

    private final MemberService memberService;
    private final CompanyRepository companyRepository;


    @PreAuthorize("hasAnyRole('SUPER','ADMIN')")
    @PostMapping
    public ApiResponse<String> register(
            @RequestBody @Valid MemberRegister memberRegister,
            @AuthenticationPrincipal MemberPrincipalDetails memberDetails) {

        String data = memberService.register(memberRegister, memberDetails.getMember().getRole());
        return ApiResponse.successResponse(OK, "성공적으로 계정 생성이 완료되었습니다.", data);
    }


    @PostMapping("/form")
    public ApiResponse<Void> formRegister(@RequestBody @Valid MemberFormRegister registerForm) {
        memberService.formRegister(registerForm);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 폼을 제출하였습니다.");
    }

    @PreAuthorize("hasRole('SUPER')")
    @GetMapping("/form")
    public ApiResponse<Page<MemberFormInfo>> getFormList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MemberFormInfo> data = memberService.findAllForms(page, size);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 목록을 조회하였습니다.",
                data
        );

    }

    @PreAuthorize("hasRole('SUPER')")
    @PatchMapping("/form")
    public ApiResponse<String> formUpdate(@RequestBody @Valid MemberFormDecision formDecision) {
        String data = memberService.updateForm(formDecision);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 처리되었습니다.",
                data);
    }

    @GetMapping("/email-check")
    public ApiResponse<Boolean> emailValidate(
            @RequestParam @NotBlank(message = "이메일이 빈 상태 입니다.")
            @Email(message = "유효한 이메일 형식이 아닙니다.") String email) {
        Boolean data = memberService.checkEmailDuplicate(email);
        return ApiResponse.successResponse(
                OK,
                "회원가입이 가능한 이메일입니다.",
                data
        );
    }


    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PatchMapping
    public ApiResponse<String> updatePassword(
            @RequestParam @Valid String newPassword,
            @AuthenticationPrincipal MemberPrincipalDetails memberDetails) {
        Long memberId = memberDetails.getMember().getId();

        memberService.updatePassword(memberId, newPassword);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 비밀번호 수정이 완료되었습니다."
        );
    }

    //TODO : 차후 리팩토링을 통해 n일 후 삭제가 '스케줄러' 등으로 'hard 삭제'가 구현되어야 합니다.
    @PreAuthorize("hasAnyRole('SUPER','ADMIN')")
    @DeleteMapping
    public ApiResponse<Void> softDeleteMember(
            @RequestParam @NotBlank String loginId,
            @AuthenticationPrincipal MemberPrincipalDetails memberDetails) {

        Member member = memberDetails.getMember();
        Role memberRole = member.getRole();
        Long companyId = member.getCompany().getId();

        MemberRemove request = MemberRemove.builder()
                .loginId(loginId)
                .companyId(companyId)
                .role(memberRole)
                .build();

        memberService.deleteMember(request);
        return ApiResponse.successResponse(OK, "성공적으로 회원을 삭제하였습니다.");
    }

    @PreAuthorize("hasAnyRole('SUPER','ADMIN')")
    @GetMapping
    public ApiResponse<List<MemberInfo>> memberAllList(
            @RequestBody MemberSearchFilter request,
            @AuthenticationPrincipal MemberPrincipalDetails memberDetails) {

        Role ownRole = memberDetails.getMember().getRole();
        Long ownCompanyId = memberDetails.getMember().getCompany().getId();

        List<MemberInfo> data = memberService.findAllMembers(ownCompanyId, ownRole, request);
        return ApiResponse.successResponse(
                OK,
                "성공적으로 유저 목록을 조회하였습니다.",
                data
        );
    }
}

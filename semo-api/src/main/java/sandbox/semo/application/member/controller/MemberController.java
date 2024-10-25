package sandbox.semo.application.member.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
import sandbox.semo.domain.member.dto.request.MemberFormDecision;
import sandbox.semo.domain.member.dto.request.MemberFormRegister;
import sandbox.semo.domain.member.dto.request.MemberRegister;
import sandbox.semo.domain.member.dto.response.MemberFormInfo;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
@Validated
public class MemberController {

    private final MemberService memberService;


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
                "성공적으로 이메일을 조회하였습니다.",
                data
        );
    }
}

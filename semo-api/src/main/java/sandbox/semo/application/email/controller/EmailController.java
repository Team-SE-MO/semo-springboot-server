package sandbox.semo.application.email.controller;

import static org.springframework.http.HttpStatus.OK;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.email.service.EmailService;
import sandbox.semo.domain.company.dto.response.CompanyFormInfo;
import sandbox.semo.domain.member.dto.request.EmailRegister;
import sandbox.semo.domain.member.dto.response.MemberRegisterRejection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
public class EmailController {

    private final EmailService emailService;

    // 비밀번호 재설정 인증코드 발송 API
    @PostMapping("/code/send")
    public ApiResponse<String> sendEmail(@RequestBody EmailRegister emailRegister) {

        // 인증 코드 이메일 발송 및 세션에 저장
        String authCode = emailService.sendAuthCode(emailRegister);
        return ApiResponse.successResponse(OK, "이메일 전송 성공", authCode);

    }

    // 인증 코드 검증 API
    @PostMapping("/code/verify")
    public ApiResponse<String> verifyAuthCode(@RequestBody String inputAuthCode) {
        emailService.verifyAuthCode(inputAuthCode);
        return ApiResponse.successResponse(OK,"인증 성공");

    }

    // 회사등록 완료 이메일 발송 API
    @PostMapping("/company/registration/{companyId}")
    public ApiResponse<String> sendCompanyRegistrationConfirmationEmail(@RequestBody CompanyFormInfo companyFormInfo) {

        // 회사등록 완료 이메일 발송
        emailService.sendCompanyRegistrationConfirmationEmail(companyFormInfo);

        // 성공 응답
        return ApiResponse.successResponse(OK, "회사등록 완료 이메일 전송 성공");
    }

    // 회원가입 완료 확인 이메일 발송 API
    @PostMapping("/registration/{loginId}")
    public ApiResponse<String> sendRegistrationConfirmationEmail(@PathVariable String loginId) {
        // 회원가입 완료 이메일 발송
        emailService.sendMemberRegistrationConfirmationEmail(loginId);
        // 성공 응답
        return ApiResponse.successResponse(OK, "회원가입 확인 이메일 전송 성공");
    }

    // 회원가입 반려 이메일 발송 API
    @PostMapping("/reject")
    public ApiResponse<String> sendMemberRegistrationRejectionEmail(@RequestBody MemberRegisterRejection memberRegisterRejection) {

        // 회원가입 반려 이메일 발송
        emailService.sendMemberRegistrationRejectionEmail(memberRegisterRejection);

        // 성공 응답
        return ApiResponse.successResponse(OK, "회원가입 반려 이메일 전송 성공");
    }
}

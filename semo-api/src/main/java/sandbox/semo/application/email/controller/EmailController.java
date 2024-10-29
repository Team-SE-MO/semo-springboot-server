package sandbox.semo.application.email.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.email.service.EmailService;
import sandbox.semo.domain.member.dto.request.EmailRegister;
import sandbox.semo.domain.member.dto.response.MemberRegisterRejection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
public class EmailController {

    private final EmailService emailService;

    // 비밀번호 재설정 인증코드 발송 API
    @PostMapping("/code")
    public ApiResponse<String> sendEmail(@Valid @RequestBody EmailRegister emailRegister) {

        // 인증 코드 이메일 발송 및 세션에 저장
        String authCode = emailService.sendAuthCode(emailRegister);
        return ApiResponse.successResponse(OK, "이메일 전송 성공", authCode);

    }

    // 인증 코드 검증 API
    @PostMapping("/code/verify")
    //public ApiResponse<Object> verifyAuthCode(@RequestBody EmailAuthCode emailAuthCode) {
    public ApiResponse<String> verifyAuthCode(@RequestBody EmailRegister emailRegister) {
    //public ApiResponse<String> verifyAuthCode(@RequestBody String inputAuthCode) {
        //emailService.verifyAuthCode(inputAuthCode);
        String email = emailRegister.getEmail(); // 이메일을 요청 본문에서 가져옴
        String inputAuthCode = emailRegister.getAuthCode();
        //emailService.verifyAuthCode(emailRegister.getEmail(), emailRegister.getAuthCode());
        emailService.verifyAuthCode(email, inputAuthCode);
        return ApiResponse.successResponse(OK,"인증 성공");

    }

    // 회사등록 완료 이메일 발송 API
    @PostMapping("/company/registration/{formId}")
    public ApiResponse<Map<String, Object>> sendCompanyRegistrationConfirmationEmail(@PathVariable Long formId) {
        Map<String, Object> emailData = emailService.sendCompanyRegistrationConfirmationEmail(formId);

        // 회사등록 완료 이메일 발송
        emailService.sendCompanyRegistrationConfirmationEmail(formId);

        // 성공 응답
        return ApiResponse.successResponse(OK, "회사등록 완료 이메일 전송 성공", emailData);
    }

    // 회원가입 완료 확인 이메일 발송 API
    @PostMapping("/registration/{loginId}")
    public ApiResponse<Map<String, Object>> sendRegistrationConfirmationEmail(@PathVariable String loginId) {
    //public ApiResponse<String> sendRegistrationConfirmationEmail(@PathVariable String loginId) {
        Map<String, Object> emailData = emailService.sendMemberRegistrationConfirmationEmail(loginId);
        // 회원가입 완료 이메일 발송
        emailService.sendMemberRegistrationConfirmationEmail(loginId);
        // 성공 응답
        //return ApiResponse.successResponse(OK, "회원가입 확인 이메일 전송 성공");
        return ApiResponse.successResponse(OK, "회원가입 확인 이메일 전송 성공", emailData);
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

package sandbox.semo.application.member.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.member.service.EmailService;
import sandbox.semo.domain.form.dto.response.CompanyRegister;
import sandbox.semo.domain.member.dto.request.EmailRegister;
import sandbox.semo.domain.member.dto.response.MemberRegister;
import sandbox.semo.domain.member.dto.response.MemberRegisterRejection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
public class EmailController {

    private final EmailService emailService;
    private final HttpSession session;

    // 비밀번호 재설정 인증코드 발송 API
    @PostMapping("/auth-code/send")
    public ApiResponse<String> sendEmail(@RequestBody EmailRegister emailRegister)
            throws MessagingException, IOException {

        // 인증 코드 생성
        String authCode = emailService.generateAuthCode();

        // 인증 코드 이메일 발송
        emailService.sendEmail(emailRegister, authCode);

        // 생성된 인증 코드를 세션에 저장
        session.setAttribute("authCode", authCode);

        // 성공 응답 (ApiResponse 객체로 성공 메시지 전송)
        return ApiResponse.successResponse(OK, "이메일 전송 성공", authCode);
    }

    // 인증 코드 검증 API
    @PostMapping("/auth-code/verify")
    public ApiResponse<String> verifyAuthCode(@RequestBody String inputAuthCode) {
        return emailService.verifyAuthCode(inputAuthCode);
    }

    // 회사등록 완료 이메일 발송 API
    @PostMapping("/company/registration-confirm")
    public ApiResponse<String> sendCompanyRegistrationConfirmationEmail(@RequestBody CompanyRegister companyFormRegister)
            throws MessagingException, IOException {

        // 회사등록 완료 이메일 발송
        emailService.sendCompanyRegistrationConfirmationEmail(companyFormRegister);

        // 성공 응답
        return ApiResponse.successResponse(OK, "회사등록 완료 이메일 전송 성공");
    }

    // 회원가입 완료 확인 이메일 발송 API
    @PostMapping("/registration/confirm")
    public ApiResponse<String> sendRegistrationConfirmationEmail(@RequestBody MemberRegister memberRegister)
            throws MessagingException, IOException {

        // 회원가입 완료 이메일 발송
        emailService.sendMemberRegistrationConfirmationEmail(memberRegister);

        // 성공 응답
        return ApiResponse.successResponse(OK, "회원가입 확인 이메일 전송 성공");
    }

    // 회원가입 반려 이메일 발송 API
    @PostMapping("/registration/reject")
    public ApiResponse<String> sendMemberRegistrationRejectionEmail(@RequestBody MemberRegisterRejection memberRegisterRejection)
            throws MessagingException, IOException {

        // 회원가입 반려 이메일 발송
        emailService.sendMemberRegistrationRejectionEmail(memberRegisterRejection);

        // 성공 응답
        return ApiResponse.successResponse(OK, "회원가입 반려 이메일 전송 성공");
    }
}

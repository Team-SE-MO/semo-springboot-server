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
import sandbox.semo.domain.form.dto.response.CompanyFormRegister;
import sandbox.semo.domain.member.dto.request.EmailRegister;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class EmailController {

    private final EmailService emailService;
    private final HttpSession session;

    //비밀번호 재설정 인증코드 발송 API
    @PostMapping("/send-auth-code")
    public ApiResponse<String> sendEmail(@RequestBody EmailRegister emailRegister)
            throws MessagingException, IOException {

        // 인증 코드 생성
        String authCode = emailService.generateAuthCode();

        // 인증 코드 이메일 발송
        emailService.sendEmail(emailRegister, "인증 코드", authCode);

        // 생성된 인증 코드를 세션에 저장
        session.setAttribute("authCode", authCode);

        // 성공 응답 (ApiResponse 객체로 성공 메시지 전송)
        return ApiResponse.successResponse(
                OK,
                "이메일 전송 성공",
                authCode
        );
    }

    // 인증 코드 검증 API
    @PostMapping("/verify-auth-code")
    public ApiResponse<String> verifyAuthCode(@RequestBody String inputAuthCode) {

        // 세션에 저장된 인증 코드 가져오기
        String storedAuthCode = (String) session.getAttribute("authCode");

        // 인증 코드 검증
        if (storedAuthCode != null && storedAuthCode.equals(inputAuthCode)) {
            return ApiResponse.successResponse(OK, "인증 성공", null);
        } else {
            return ApiResponse.errorResponse(400, "인증코드가 일치하지 않습니다.");
        }
    }

    // 회원가입 완료 이메일 발송 API
    @PostMapping("/send-registration-confirmation")
    public ApiResponse<String> sendRegistrationConfirmationEmail(@RequestBody CompanyFormRegister companyFormRegister)
            throws MessagingException, IOException {

        // 회원가입 완료 이메일 발송
        emailService.sendRegistrationConfirmationEmail(companyFormRegister);

        // 성공 응답
        return ApiResponse.successResponse(OK, "회원가입 완료 이메일 전송 성공", null);
    }
}

package sandbox.semo.application.email.controller;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.email.service.EmailService;
import sandbox.semo.domain.member.dto.request.EmailAuthVerify;
import sandbox.semo.domain.member.dto.request.EmailSendRequest;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
public class EmailController {

    private final EmailService emailService;

    @PostMapping
    public ApiResponse<Map<String, Object>> processEmailRequest(@Valid @RequestBody EmailSendRequest request) {
        Map<String, Object> emailData = emailService.processEmailRequest(request);
        return ApiResponse.successResponse(HttpStatus.OK, "이메일 전송 성공", emailData);
    }

    @PostMapping("/auth")
    public ApiResponse<String> verifyEmailAuthCode(@RequestBody EmailAuthVerify verify) {
        emailService.verifyEmailAuthCode(verify);
        return ApiResponse.successResponse(HttpStatus.OK, "인증코드 검증 성공");
    }

}

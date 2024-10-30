package sandbox.semo.application.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.email.service.EmailService;
import sandbox.semo.domain.member.dto.request.EmailSend;
import sandbox.semo.domain.member.dto.request.EmailAuthVerify;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
public class EmailController {

    private final EmailService emailService;

    @PostMapping
    public ApiResponse<String> processEmailRequest(@Valid @RequestBody EmailSend request) {
        emailService.processEmailRequest(request);
        return ApiResponse.successResponse(HttpStatus.OK, "이메일 전송 성공");
    }

    @PostMapping("/auth")
    public ApiResponse<String> verifyAuthCode(@RequestBody EmailAuthVerify emailRequest) {
        emailService.verifyAuthCode(emailRequest.getEmail(), emailRequest.getAuthCode());
        return ApiResponse.successResponse(HttpStatus.OK, "인증코드 검증 성공");
    }

}

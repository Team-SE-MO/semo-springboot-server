package sandbox.semo.application.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.application.email.service.EmailService;
import sandbox.semo.domain.member.dto.request.EmailRequest;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mail")
public class EmailController {

    private final EmailService emailService;

    // 통합 이메일 발송 API
    @PostMapping("/{apiType}")
    public ApiResponse<String> sendEmail(@PathVariable String apiType, @Valid @RequestBody EmailRequest emailRequest) {
        emailRequest.setApiType(apiType);

        emailService.processEmailRequest(emailRequest);
        return ApiResponse.successResponse(HttpStatus.OK, "이메일 전송 성공");
    }

    // 인증 코드 검증 API
    @PostMapping("/auth")
    public ApiResponse<String> verifyAuthCode(@RequestBody EmailRequest emailRequest) {
        emailService.verifyAuthCode(emailRequest.getEmail(), emailRequest.getAuthCode());
        return ApiResponse.successResponse(HttpStatus.OK, "인증코드 검증 성공");
    }

}

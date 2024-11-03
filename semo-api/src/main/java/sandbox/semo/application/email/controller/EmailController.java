package sandbox.semo.application.email.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
    public ApiResponse<String> processEmailRequest(@Valid @RequestBody EmailSendRequest request) {
        String successMessage = emailService.processEmailRequest(request);
        return ApiResponse.successResponse(OK, successMessage);
    }

    @PostMapping("/auth")
    public ApiResponse<String> verifyEmailAuthCode(@Valid @RequestBody EmailAuthVerify request) {
        emailService.verifyEmailAuthCode(request);
        return ApiResponse.successResponse(OK, "인증코드 검증 성공");
    }

}

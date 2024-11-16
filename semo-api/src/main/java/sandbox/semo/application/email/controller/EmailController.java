package sandbox.semo.application.email.controller;

import static org.springframework.http.HttpStatus.OK;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping("/auth")
    public ApiResponse<Void> sendAuthCode(@RequestParam String email) {
        emailService.sendEmailAuthCode(email);
        return ApiResponse.successResponse(OK, "성공적으로 인증 코드를 전송 하였습니다.");
    }

    @PostMapping("/valid")
    public ApiResponse<String> verifyEmailAuthCode(@Valid @RequestBody EmailAuthVerify request) {
        emailService.verifyEmailAuthCode(request);
        return ApiResponse.successResponse(OK, "성공적으로 인증 코드 검증이 완료 되었습니다.");
    }

    @PreAuthorize("hasAnyRole('SUPER','ADMIN')")
    @PostMapping
    public ApiResponse<String> processEmailRequest(@Valid @RequestBody EmailSendRequest request) {
        String successMessage = emailService.processEmailRequest(request);
        return ApiResponse.successResponse(OK, successMessage);
    }

}

package sandbox.semo.application.email.service;

import sandbox.semo.domain.member.dto.request.EmailAuthVerify;
import sandbox.semo.domain.member.dto.request.EmailSendRequest;

public interface EmailService {

    String processEmailRequest(EmailSendRequest request);

    void sendEmailAuthCode(String request);

    void verifyEmailAuthCode(EmailAuthVerify request);
}

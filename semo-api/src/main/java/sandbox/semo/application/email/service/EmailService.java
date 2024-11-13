package sandbox.semo.application.email.service;

import sandbox.semo.domain.member.dto.request.EmailAuthVerify;
import sandbox.semo.domain.member.dto.request.EmailSendRequest;

public interface EmailService {

    void sendEmailAuthCode(String request);

    void verifyEmailAuthCode(EmailAuthVerify request);

    String processEmailRequest(EmailSendRequest request);

}

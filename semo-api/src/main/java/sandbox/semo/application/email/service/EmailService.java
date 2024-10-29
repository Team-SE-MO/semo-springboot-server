package sandbox.semo.application.email.service;

import sandbox.semo.domain.member.dto.request.EmailSend;

public interface EmailService {

    void sendEmail(String email, String text);

    void verifyAuthCode(String email, String authCode);

    void processEmailRequest(EmailSend request);

}

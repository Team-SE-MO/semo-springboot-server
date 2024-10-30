package sandbox.semo.application.email.service;

import sandbox.semo.domain.member.dto.request.EmailSend;

public interface EmailService {

    void processEmailRequest(EmailSend request);

    void verifyAuthCode(String email, String authCode);

}

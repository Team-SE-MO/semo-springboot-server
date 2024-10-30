package sandbox.semo.application.email.service;

import java.util.Map;
import sandbox.semo.domain.member.dto.request.EmailAuthVerify;
import sandbox.semo.domain.member.dto.request.EmailSendRequest;

public interface EmailService {

    Map<String, Object> processEmailRequest(EmailSendRequest request);

    void verifyEmailAuthCode(EmailAuthVerify verify);
}

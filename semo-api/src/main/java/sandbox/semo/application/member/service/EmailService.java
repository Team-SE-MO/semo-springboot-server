package sandbox.semo.application.member.service;

import jakarta.mail.MessagingException;
import java.io.IOException;
import sandbox.semo.member.dto.request.EmailRegister;

public interface EmailService {

    //인증코드 생성 메서드
    String generateAuthCode();

    // 이메일 발송 메서드
    void sendEmail(EmailRegister email, String subject, String text)
            throws MessagingException, IOException;
}

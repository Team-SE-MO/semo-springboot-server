package sandbox.semo.application.member.service;

import jakarta.mail.MessagingException;
import java.io.IOException;
import sandbox.semo.domain.member.dto.request.EmailRegister;
import sandbox.semo.domain.form.dto.response.CompanyFormRegister;
import sandbox.semo.domain.member.dto.response.MemberRegister;

public interface EmailService {

    //인증코드 생성 메서드
    String generateAuthCode();

    // 이메일 발송 메서드
    void sendEmail(EmailRegister email, String subject, String text)
            throws MessagingException, IOException;

    // 회사등록 완료 이메일 발송 메서드
    void sendCompanyRegistrationConfirmationEmail(CompanyFormRegister companyFormRegister)
            throws MessagingException, IOException;

    // 회원가입 완료 이메일 발송 메서드
    void sendMemberRegistrationConfirmationEmail(MemberRegister memberRegister)
            throws  MessagingException, IOException;
}

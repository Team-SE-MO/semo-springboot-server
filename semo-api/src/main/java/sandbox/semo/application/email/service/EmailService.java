package sandbox.semo.application.email.service;

import jakarta.mail.MessagingException;
import java.io.IOException;
import sandbox.semo.application.common.response.ApiResponse;
import sandbox.semo.domain.form.dto.response.CompanyRegister;
import sandbox.semo.domain.member.dto.request.EmailRegister;
import sandbox.semo.domain.member.dto.response.MemberRegister;
import sandbox.semo.domain.member.dto.response.MemberRegisterRejection;

public interface EmailService {

    // 인증 코드 검증 메서드
    ApiResponse<String> verifyAuthCode(String inputAuthCode);

    //인증코드 생성 메서드
    String generateAuthCode();

    // 이메일 발송 메서드
    void sendEmail(EmailRegister email, String text);

    // 회사등록 완료 이메일 발송 메서드
    void sendCompanyRegistrationConfirmationEmail(CompanyRegister companyFormRegister);

    // 회원가입 완료 이메일 발송 메서드
    void sendMemberRegistrationConfirmationEmail(MemberRegister memberRegister);

    // 회원가입 반려 이메일 발송 메서드
    void sendMemberRegistrationRejectionEmail(MemberRegisterRejection memberRegisterRejection);
}

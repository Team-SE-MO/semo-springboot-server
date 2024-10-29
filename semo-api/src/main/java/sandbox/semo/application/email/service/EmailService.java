package sandbox.semo.application.email.service;

import java.util.Map;
import sandbox.semo.domain.member.dto.request.EmailRegister;
import sandbox.semo.domain.member.dto.response.MemberRegisterRejection;
import sandbox.semo.domain.member.dto.request.EmailRequest;

public interface EmailService {

    // 이메일 요청처리 메서드
    void processEmailRequest(EmailRequest emailRequest);

    // 인증 코드 검증 메서드
    void verifyAuthCode(String email, String authCode);

    // 인증 코드 발송 메서드
    String sendAuthCode(EmailRegister emailRegister);

    //인증코드 생성 메서드
    String generateAuthCode();

    // 이메일 발송 메서드
    void sendEmail(EmailRegister email, String text);

    // 회사등록 완료 이메일 발송 메서드
    Map<String, Object> sendCompanyRegistrationConfirmationEmail(Long formId);

    // 회원가입 완료 이메일 발송 메서드
    Map<String, Object> sendMemberRegistrationConfirmationEmail(String loginId);

    // 회원가입 반려 이메일 발송 메서드
    void sendMemberRegistrationRejectionEmail(MemberRegisterRejection memberRegisterRejection);

    // 회사등록 반려 이메일 발송 메서드
    void sendCompanyRegistrationRejectionEmail(String value);
}

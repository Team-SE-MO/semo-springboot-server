package sandbox.semo.application.email.service;

import static sandbox.semo.application.email.exception.EmailErrorCode.APPROVAL_DENIED;
import static sandbox.semo.application.email.exception.EmailErrorCode.COMPANY_NAME_MISSING;
import static sandbox.semo.application.email.exception.EmailErrorCode.EMAIL_SEND_FAILED;
import static sandbox.semo.application.email.exception.EmailErrorCode.EMAIL_TEMPLATE_LOAD_FAILED;
import static sandbox.semo.application.email.exception.EmailErrorCode.INVALID_AUTH_CODE;
import static sandbox.semo.application.email.exception.EmailErrorCode.INVALID_REQUEST;
import static sandbox.semo.application.email.exception.EmailErrorCode.MEMBER_NOT_FOUND;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import sandbox.semo.application.email.exception.EmailBusinessException;
import sandbox.semo.domain.common.entity.FormStatus;
import sandbox.semo.domain.company.entity.CompanyForm;
import sandbox.semo.domain.company.repository.CompanyFormRepository;
import sandbox.semo.domain.member.dto.request.EmailAuthVerify;
import sandbox.semo.domain.member.dto.request.EmailSendRequest;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.member.repository.MemberRepository;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final HttpSession session;
    private final MemberRepository memberRepository;
    private final CompanyFormRepository companyFormRepository;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.password}")
    private String password;

    @Override
    public void sendEmailAuthCode(String email) {
        sendAuthCode(email);
    }

    @Override
    public String processEmailRequest(EmailSendRequest request) {
        String apiType = request.getApiType();
        String value = request.getValue();
        String successMessage;
        log.info(">>> [ 📤 이메일 전송 요청 형식: {} ]", apiType);
        switch (apiType) {
            case "REGISTER_MEMBER" -> {
                sendMemberConfirm(value);
                successMessage = "사용자 등록 완료 이메일 전송 성공";
            }
            case "REGISTER_COMPANY" -> {
                sendCompanyConfirm(Long.parseLong(value));
                successMessage = "회사 등록 완료 이메일 전송 성공";
            }
            case "FAIL_MEMBER" -> {
                sendMemberFail(value);
                successMessage = "사용자 등록 반려 이메일 전송 성공";
            }
            case "FAIL_COMPANY" -> {
                sendCompanyFail(value);
                successMessage = "회사 등록 반려 이메일 전송 성공";
            }
            default -> throw new EmailBusinessException(INVALID_REQUEST);
        }
        log.info(">>> [ ✅ 이메일 전송 성공 ]");
        return successMessage;
    }

    @Override
    public void verifyEmailAuthCode(EmailAuthVerify request) {
        String email = request.getEmail();
        String authCode = request.getAuthCode();

        String sessionAuthCode = (String) session.getAttribute("authCode" + email);
        log.info(">>> [ 🔍 인증 코드 검증 중 - 이메일: {}, 세션 인증 코드: {}, 입력된 인증 코드: {}]",
                email, sessionAuthCode, authCode);

        if (isInvalidAuthCode(sessionAuthCode, authCode)) {
            log.error(">>> [ ❌ 인증 코드 불일치 - 이메일: {} ]", email);
            throw new EmailBusinessException(INVALID_AUTH_CODE);
        }

        log.info(">>> [ ✅ 인증 코드 검증 성공 - 이메일: {}, 인증 코드: {} ]", email, authCode);
    }

    private boolean isInvalidAuthCode(String sessionAuthCode, String authCode) {
        return sessionAuthCode == null || !sessionAuthCode.equals(authCode);
    }

    private void sendMemberConfirm(String loginId) {
        log.info(">>> [ 🔍 조회 중인 loginId: {}]", loginId);
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EmailBusinessException(MEMBER_NOT_FOUND));
        log.info(">>> [ ✅ 사용자 조회 성공 - loginId: {}]", member.getLoginId());

        String htmlContent = readHtmlTemplate("member-registration.html")
                .replace("{{ownerName}}", member.getOwnerName())
                .replace("{{loginId}}", member.getLoginId())
                .replace("{{password}}", "0000")
                .replace("{{currentDate}}",
                        new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(new Date()));

        sendMail(member.getEmail(), "[SEMO] 계정 등록이 완료 되었습니다.", htmlContent);
    }

    private void sendCompanyConfirm(Long formId) {
        log.info(">>> [ 🔍 조회 중인 formId: {}]", formId);
        CompanyForm companyForm = companyFormRepository.findById(formId)
                .orElseThrow(() -> new EmailBusinessException(COMPANY_NAME_MISSING));
        log.info(">>> [ ✅ 회사 조회 성공 - formId: {}]", companyForm.getId());

        if (companyForm.getFormStatus() != FormStatus.APPROVED) {
            log.warn(">>> [ ⛔ 이메일 전송 중지 - formStatus가 APPROVED가 아님: {} ]",
                    companyForm.getFormStatus());
            throw new EmailBusinessException(APPROVAL_DENIED);
        }

        String htmlContent = readHtmlTemplate("company-registration.html")
                .replace("{{companyName}}", companyForm.getCompanyName())
                .replace("{{ownerName}}", companyForm.getOwnerName())
                .replace("{{currentDate}}",
                        new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(new Date()));

        sendMail(companyForm.getEmail(), "[SEMO] 회사 등록이 완료 되었습니다.", htmlContent);
    }

    private void sendAuthCode(String email) {
        String authCode = generateAuthCode();
        sendAuthEmail(email, authCode);
        session.setAttribute("authCode" + email, authCode);
        session.setMaxInactiveInterval(5 * 60);
    }

    private String generateAuthCode() {
        Random random = new Random();
        int authCode = random.nextInt(999999);
        log.info(">>> [ 🔐 인증 코드 생성: {} ]", authCode);
        return String.format("%06d", authCode);
    }

    private void sendMemberFail(String email) {
        String htmlContent = readHtmlTemplate("member-rejection.html")
                .replace("{{currentDate}}",
                        new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(new Date()));

        sendMail(email, "[SEMO] 회원가입 반려 안내", htmlContent);
    }

    private void sendCompanyFail(String email) {
        String htmlContent = readHtmlTemplate("company-rejection.html")
                .replace("{{currentDate}}",
                        new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(new Date()));

        sendMail(email, "[SEMO] 회사 등록 반려 안내", htmlContent);
    }

    private String readHtmlTemplate(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + fileName);
            return new String(Files.readAllBytes(resource.getFile().toPath()), "UTF-8");
        } catch (IOException e) {
            log.error(">>> [ ❌ HTML 템플릿 로드 실패 ]", e);
            throw new EmailBusinessException(EMAIL_TEMPLATE_LOAD_FAILED);
        }
    }

    private void sendMail(String emailReceive, String subject, String htmlContent) {
        try {
            Session session = createMailSession();

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            MimeBodyPart imagePart = new MimeBodyPart();
            ClassPathResource imgResource = new ClassPathResource("img/Block.png");
            imagePart.attachFile(imgResource.getFile());
            imagePart.setContentID("<blockImage>");
            imagePart.setDisposition(MimeBodyPart.INLINE);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(imagePart);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailReceive));
            message.setSubject(subject);
            message.setContent(multipart);

            Transport.send(message);
            log.info(">>> [ ✅ 이메일 전송 성공 - 수신자: {} ]", emailReceive);
        } catch (MessagingException | IOException e) {
            log.error(">>> [ ❌ 이메일 전송 실패 - 수신자: {} ]", emailReceive, e);
            throw new EmailBusinessException(EMAIL_SEND_FAILED);
        }
    }

    private Session createMailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });
    }

    private void sendAuthEmail(String email, String authCode) {
        if (authCode == null) {
            throw new EmailBusinessException(INVALID_AUTH_CODE);
        }

        String htmlContent = readHtmlTemplate("send-auth-code.html")
                .replace("{{authCode}}", authCode)
                .replace("{{currentDate}}",
                        new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(new Date()));

        sendMail(email, "[SEMO] 이메일 인증 코드 발송.", htmlContent);
    }

}

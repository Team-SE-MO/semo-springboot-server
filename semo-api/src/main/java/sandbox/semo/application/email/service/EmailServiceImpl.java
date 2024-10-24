package sandbox.semo.application.email.service;

import static sandbox.semo.application.email.exception.EmailErrorCode.EMAIL_SEND_FAILED;

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
import sandbox.semo.application.email.exception.EmailErrorCode;
import sandbox.semo.domain.company.dto.response.CompanyFormInfo;
import sandbox.semo.domain.member.dto.request.EmailRegister;
import sandbox.semo.domain.member.dto.response.MemberRegisterRejection;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.member.repository.MemberRepository;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final HttpSession session;
    private final MemberRepository memberRepository;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.password}")
    private String password;

    @Override
    public String sendAuthCode(EmailRegister emailRegister) {
        // 인증 코드 생성
        String authCode = generateAuthCode();

        // 인증 코드 이메일 발송
        sendEmail(emailRegister, authCode);

        // 생성된 인증 코드를 세션에 저장
        session.setAttribute("authCode", authCode);

        return authCode; // 생성된 인증 코드 반환
    }

    @Override
    public void verifyAuthCode (String inputAuthCode){
        String storedAuthCode = (String) session.getAttribute("authCode");
        if(storedAuthCode == null || storedAuthCode.equals(inputAuthCode)){
            throw new EmailBusinessException(EmailErrorCode.INVALID_AUTH_CODE);
        }
    }

    @Override
    public String generateAuthCode() {
        Random random = new Random();
        int authCode = random.nextInt(999999);
        log.info(">>> [ 🔐 인증 코드 생성: {} ]", authCode);
        return String.format("%06d", authCode);
    }

    private String readHtmlTemplate(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + fileName);
            return new String(Files.readAllBytes(resource.getFile().toPath()), "UTF-8");
        } catch (IOException e) {
            log.error(">>> [ ❌ HTML 템플릿 로드 실패 ]", e);
            throw new EmailBusinessException(EmailErrorCode.EMAIL_TEMPLATE_LOAD_FAILED);
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

    private void sendMail(String to, String subject, String htmlContent) {
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(multipart);

            Transport.send(message);
            log.info(">>> [ ✅ 이메일 전송 성공 - 수신자: {} ]", to);
        } catch (MessagingException | IOException e) {
            log.error(">>> [ ❌ 이메일 전송 실패 - 수신자: {} ]", to, e);
            throw new EmailBusinessException(EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void sendEmail(EmailRegister email, String authCode) {
        if (authCode == null) {
            throw new EmailBusinessException(EmailErrorCode.INVALID_AUTH_CODE); // authCode가 null인 경우 예외 발생
        }

        String htmlContent = readHtmlTemplate("send-auth-code.html")
                .replace("{{authCode}}", authCode)
                .replace("{{currentDate}}", new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(new Date()));

        sendMail(email.getEmail(), "[SEMO] 비밀번호를 재설정 해주세요.", htmlContent);
    }

    @Override
    public void sendCompanyRegistrationConfirmationEmail(CompanyFormInfo companyFormInfo) {
        if (companyFormInfo.getCompanyName() == null || companyFormInfo.getCompanyName().isEmpty()) {
            throw new EmailBusinessException(EmailErrorCode.COMPANY_NAME_MISSING);
        }

        if (companyFormInfo.getOwnerName() == null || companyFormInfo.getOwnerName().isEmpty()) {
            throw new EmailBusinessException(EmailErrorCode.OWNER_NAME_MISSING);
        }

        String htmlContent = readHtmlTemplate("company-registration.html")
                .replace("{{companyName}}", companyFormInfo.getCompanyName())
                .replace("{{ownerName}}", companyFormInfo.getOwnerName())
                .replace("{{currentDate}}", new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(new Date()));

        sendMail(companyFormInfo.getEmail(), "[SEMO] 회사 등록이 완료되었습니다.", htmlContent);
    }

    @Override
    public void sendMemberRegistrationConfirmationEmail(String loginId) {
        log.info(">>> [🔍 조회 중인 loginId: {}]", loginId);
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EmailBusinessException(EmailErrorCode.MEMBER_NOT_FOUND)); // 값이 없을 경우 예외 처리
        log.info(">>> [✅ 사용자 조회 성공 - loginId: {}]", member.getLoginId());

        String htmlContent = readHtmlTemplate("member-registration.html")
                .replace("{{ownerName}}", member.getOwnerName())
                .replace("{{loginId}}", member.getLoginId())
                .replace("{{password}}", "0000")
                .replace("{{currentDate}}", new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(new Date()));

        sendMail(member.getEmail(), "[SEMO] 계정 등록이 완료되었습니다.", htmlContent);
    }

    @Override
    public void sendMemberRegistrationRejectionEmail(MemberRegisterRejection memberRegisterRejection) {
        String htmlContent = readHtmlTemplate("member-rejection.html")
                .replace("{{currentDate}}", new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분").format(new Date()));

        sendMail(memberRegisterRejection.getEmail(), "[SEMO] 회원가입 반려 안내", htmlContent);
    }
}

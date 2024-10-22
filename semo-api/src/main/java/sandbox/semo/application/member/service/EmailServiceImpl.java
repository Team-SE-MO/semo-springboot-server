package sandbox.semo.application.member.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sandbox.semo.domain.form.dto.response.CompanyFormRegister;
import sandbox.semo.domain.member.dto.request.EmailRegister;
import sandbox.semo.domain.member.dto.response.MemberRegister;
import sandbox.semo.domain.member.dto.response.MemberRegisterRejection;
import sandbox.semo.application.common.response.ApiResponse;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final HttpSession session;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.password}")
    private String password;

    @Override
    public ApiResponse<String> verifyAuthCode(String inputAuthCode) {
        // 세션에 저장된 인증 코드 가져오기
        String storedAuthCode = (String) session.getAttribute("authCode");

        // 인증 코드 검증
        if (storedAuthCode != null && storedAuthCode.equals(inputAuthCode)) {
            return ApiResponse.successResponse(HttpStatus.OK, "인증 성공", null);
        } else {
            return ApiResponse.errorResponse(400, "인증코드가 일치하지 않습니다.");
        }
    }

    @Override
    public String generateAuthCode() {
        Random random = new Random();
        int authCode = random.nextInt(999999);
        log.info(">>> [ 🔐 인증 코드 생성: {} ]", authCode);
        return String.format("%06d", authCode);
    }

    // HTML 파일을 문자열로 읽어오는 유틸리티 메서드
    private String readHtmlTemplate(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/" + fileName);
        return new String(Files.readAllBytes(resource.getFile().toPath()), "UTF-8");
    }

    // 이메일 발송 메서드: 수신자, 제목, 인증코드 받아서 이메일 발송
    @Override
    public void sendEmail(EmailRegister email, String subject, String authCode)
            throws MessagingException, IOException {
        String to = email.getEmail();
        log.info(">>> [ 📧 이메일 발송 준비 - 수신자: {} 제목: {} ]", to, subject);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
        String currentDate = dateFormat.format(new Date());

        // Gmail SMTP 설정
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        // 이메일 템플릿 읽어오기
        String htmlContent = readHtmlTemplate("send-auth-code.html");
        // 동적으로 필요한 값 삽입
        htmlContent = htmlContent.replace("{{authCode}}", authCode)
                .replace("{{currentDate}}", currentDate);

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
        message.setSubject("[SEMO]비밀번호를 재설정 해주세요."); // 제목 설정
        message.setContent(multipart);

        try {
            Transport.send(message);
            log.info(">>> [ ✅ 이메일 전송 성공 - 수신자: {} ]", to);
        } catch (MessagingException e) {
            log.error(">>> [ ❌ 이메일 전송 실패: {} ]", e.getMessage());
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendCompanyRegistrationConfirmationEmail(CompanyFormRegister companyFormRegister)
            throws MessagingException, IOException {
        String to = companyFormRegister.getEmail();
        String subject = "[SEMO] 회사 등록이 완료되었습니다.";
        log.info(">>> [ 📧 회사등록 완료 이메일 발송 준비 - 수신자: {} 제목: {} ]", to, subject);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
        String currentDate = dateFormat.format(new Date());

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        String htmlContent = readHtmlTemplate("company-registration.html");
        htmlContent = htmlContent.replace("{{companyName}}", companyFormRegister.getCompanyName())
                .replace("{{ownerName}}", companyFormRegister.getOwnerName())
                .replace("{{currentDate}}", currentDate);

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

        try {
            Transport.send(message);
            log.info(">>> [ ✅ 회사등록 완료 이메일 전송 성공 - 수신자: {} ]", to);
        } catch (MessagingException e) {
            log.error(">>> [ ❌ 이메일 전송 실패: {} ]", e.getMessage());
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMemberRegistrationConfirmationEmail(MemberRegister memberRegister)
            throws MessagingException, IOException {
        String to = memberRegister.getEmail();
        String subject = "[SEMO] 계정 등록이 완료되었습니다.";
        log.info(">>> [ 📧 회원가입 완료 이메일 발송 준비 - 수신자: {} 제목: {} ]", to, subject);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
        String currentDate = dateFormat.format(new Date());

        // 이메일 세션 설정
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        // 이메일 템플릿 읽어오기
        String htmlContent = readHtmlTemplate("member-registration.html");

        // 동적으로 필요한 값 삽입
        htmlContent = htmlContent.replace("{{ownerName}}", memberRegister.getOwnerName())
                .replace("{{loginId}}", memberRegister.getLoginId())
                .replace("{{password}}", memberRegister.getPassword())
                .replace("{{currentDate}}", currentDate);

        // 이메일 본문 작성
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

        // 이메일에 첨부할 이미지 파일 설정
        MimeBodyPart imagePart = new MimeBodyPart();
        ClassPathResource imgResource = new ClassPathResource("img/Block.png");
        imagePart.attachFile(imgResource.getFile());
        imagePart.setContentID("<blockImage>");
        imagePart.setDisposition(MimeBodyPart.INLINE);

        // 멀티파트로 구성된 이메일 설정
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(imagePart);

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(multipart);

        // 이메일 발송
        try {
            Transport.send(message);
            log.info(">>> [ ✅ 회원가입 완료 이메일 전송 성공 - 수신자: {} ]", to);
        } catch (MessagingException e) {
            log.error(">>> [ ❌ 이메일 전송 실패: {} ]", e.getMessage());
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMemberRegistrationRejectionEmail(MemberRegisterRejection memberRegisterRejection)
            throws MessagingException, IOException {

        String to = memberRegisterRejection.getEmail(); // 수신자의 이메일 주소 설정
        String subject = "[SEMO] 회원가입 반려 안내"; // 이메일 제목

        // 이메일 발송 정보 로그 출력
        log.info(">>> [ 📧 회원가입 반려 이메일 발송 준비 - 수신자: {} 제목: {} ]", to, subject);

        // 현재 날짜를 원하는 형식으로 포맷팅
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분");
        String currentDate = dateFormat.format(new Date());

        // Gmail SMTP 서버 설정
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // SMTP 인증을 위한 세션 생성
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        // 이메일 템플릿 파일 읽어오기
        String htmlContent = readHtmlTemplate("member-rejection.html");

        // 동적으로 필요한 값 삽입
        htmlContent = htmlContent.replace("{{currentDate}}", currentDate);

        // MIME 메시지 작성
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

        // 이미지 첨부를 위한 BodyPart
        MimeBodyPart imagePart = new MimeBodyPart();
        ClassPathResource imgResource = new ClassPathResource("img/Block.png"); // 이미지 파일 경로
        imagePart.attachFile(imgResource.getFile()); // 이미지 파일 첨부
        imagePart.setContentID("<blockImage>"); // 이미지 CID 설정
        imagePart.setDisposition(MimeBodyPart.INLINE); // 이미지 인라인 설정

        // 멀티파트로 이메일 구성
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart); // HTML 본문 추가
        multipart.addBodyPart(imagePart); // 이미지 추가

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from)); // 발신자 설정
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 수신자 설정
        message.setSubject(subject); // 제목 설정
        message.setContent(multipart); // 멀티파트로 설정

        try {
            // 이메일 전송
            Transport.send(message);
            // 성공 로그 출력
            log.info(">>> [ ✅ 회원가입 반려 이메일 전송 성공 - 수신자: {} ]", to);
        } catch (MessagingException e) {
            // 에러 로그 출력 및 예외 발생
            log.error(">>> [ ❌ 이메일 전송 실패: {} ]", e.getMessage());
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }


}

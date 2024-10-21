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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import sandbox.semo.domain.member.dto.request.EmailRegister;
import sandbox.semo.domain.form.dto.response.CompanyFormRegister;
import sandbox.semo.domain.member.dto.response.MemberRegister;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username}")  // 이메일 발신자의 주소
    private String from;

    @Value("${spring.mail.password}") // 이메일 발신자의 비밀번호
    private String password;

    // 인증코드 생성 메서드: 6자리 숫자 랜덤 생성
    @Override
    public String generateAuthCode() {
        Random random = new Random();
        int authCode = random.nextInt(999999); // 6자리 인증 코드 생성
        log.info(">>> [ 🔐 인증 코드 생성: {} ]", authCode);  // 생성된 인증 코드 로그 출력
        return String.format("%06d", authCode);
    }

    // 이메일 발송 메서드: 수신자, 제목, 본문을 받아서 이메일 발송
    @Override
    public void sendEmail(EmailRegister email, String subject, String authCode)
            throws MessagingException, IOException {
        String to = email.getEmail();  // 수신자의 이메일 주소 가져오기

        // 이메일 발송 정보 로그 출력
        log.info(">>> [ 📧 이메일 발송 준비 - 수신자: {} 제목: {} ]", to, subject);

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

        // MIME 멀티파트 메시지 작성 (이미지와 HTML 내용 포함)
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from)); // 발신자 설정
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 수신자 설정
        message.setSubject("[SEMO]비밀번호를 재설정 해주세요."); // 제목 설정

        // 이메일 본문을 담을 HTML 내용
        String htmlContent = "<html>"
                + "<body>"
                + "<img src='cid:blockImage' />" // 이미지 삽입
                + "<h2>Hello, We are SEMO!</h2>"
                + "<p>SEMO 서비스를 이용해주시는 고객 여러분 감사합니다.</p>"
                + "<p>비밀번호 재설정을 위한 인증 코드는 아래와 같습니다.</p>"
                + "<br>"
                + "<a href='#' style='background-color:#64739f; color:white; padding:10px 135px; text-decoration:none; border-radius:50px; font-weight:bold;'>"
                + authCode + "</a>" // 인증 코드를 버튼 스타일로 강조
                + "<br><br>"
                + "<p style='font-size:10px;'>본 메일은 " + currentDate + " 기준으로 작성되었습니다.</p>"
                + "</body>"
                + "</html>";

        // MIME 메시지 작성
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

        // 이미지 첨부를 위한 BodyPart
        MimeBodyPart imagePart = new MimeBodyPart();
        ClassPathResource imgResource = new ClassPathResource("img/Block.png"); // 이미지 파일 경로
        imagePart.attachFile(imgResource.getFile()); // 이미지 파일 첨부
        imagePart.setContentID("<blockImage>"); // 이미지 CID 설정
        imagePart.setDisposition(MimeBodyPart.INLINE); // 이미지 인라인 설정

        // 멀티파트로 이메일 구성 (이미지 + 본문)
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart); // HTML 본문 추가
        multipart.addBodyPart(imagePart); // 이미지 추가

        message.setContent(multipart); // 멀티파트로 설정

        try {
            // 이메일 전송
            Transport.send(message);
            // 성공 로그 출력
            log.info(">>> [ ✅ 이메일 전송 성공 - 수신자: {} ]", to);
        } catch (MessagingException e) {
            // 에러 로그 출력 및 예외 발생
            log.error(">>> [ ❌ 이메일 전송 실패: {} ]", e.getMessage());
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }

    // 회사등록 완료 이메일 발송 메서드: 고객사 회사등록 정보를 사용하여 이메일 발송
    @Override
    public void sendCompanyRegistrationConfirmationEmail(CompanyFormRegister companyFormRegister)
            throws MessagingException, IOException {
        String to = companyFormRegister.getEmail(); // 수신자의 이메일 주소 가져오기
        String subject = "[SEMO] 회사 등록이 완료되었습니다."; // 이메일 제목

        // 이메일 발송 정보 로그 출력
        log.info(">>> [ 📧 회원가입 완료 이메일 발송 준비 - 수신자: {} 제목: {} ]", to, subject);

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

        // MIME 멀티파트 메시지 작성
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from)); // 발신자 설정
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 수신자 설정
        message.setSubject(subject); // 제목 설정

        // 이메일 본문을 담을 HTML 내용
        String htmlContent = "<html>"
                + "<body>"
                + "<img src='cid:blockImage' />" // 이미지 삽입
                + "<h2>Hello, We are SEMO!</h2>"
                + "<p>" + companyFormRegister.getOwnerName() + "님 안녕하세요, SEMO 서비스를 이용해주셔서 감사드립니다.</p>"
                + "<p>아래와 같은 회사명으로 회사가 등록되었습니다. 이어서 SEMO 서비스 등록 신청을 이용해주세요.</p>"
                + "<br>"
                + "<a href='#' style='background-color:#64739f; color:white; padding:10px 135px; text-decoration:none; border-radius:50px; font-weight:bold;'>"
                + companyFormRegister.getCompanyName() + "</a>"
                + "<br><br>"
                + "<p>SEMO 는 서비스를 더욱 편리하게 이용하실 수 있도록 항상 최선을 다하겠습니다.</p>"
                + "<br>"
                + "<p>감사합니다.</p>"
                + "<br><br>"
                + "<p style='font-size:10px;'>본 메일은 " + currentDate + " 기준으로 작성되었습니다.</p>"
                + "</body>"
                + "</html>";

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
        message.setContent(multipart); // 멀티파트로 설정

        try {
            // 이메일 전송
            Transport.send(message);
            // 성공 로그 출력
            log.info(">>> [ ✅ 회원가입 완료 이메일 전송 성공 - 수신자: {} ]", to);
        } catch (MessagingException e) {
            // 에러 로그 출력 및 예외 발생
            log.error(">>> [ ❌ 이메일 전송 실패: {} ]", e.getMessage());
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }

    // 회원가입 완료 이메일 발송 메서드: 회원가입 정보를 사용하여 이메일 발송
    @Override
    public void sendMemberRegistrationConfirmationEmail(MemberRegister memberRegister)
            throws MessagingException, IOException {

        String to = memberRegister.getEmail(); // 수신자의 이메일 주소 설정
        String subject = "[SEMO] 계정 등록이 완료되었습니다."; // 이메일 제목

        // 이메일 발송 정보 로그 출력
        log.info(">>> [ 📧 회원가입 완료 이메일 발송 준비 - 수신자: {} 제목: {} ]", to, subject);

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

        // MIME 멀티파트 메시지 작성
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from)); // 발신자 설정
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to)); // 수신자 설정
        message.setSubject(subject); // 제목 설정

        // 이메일 본문을 담을 HTML 내용
        String htmlContent = "<html>"
                + "<body>"
                + "<img src='cid:blockImage' />" // 이미지 삽입
                + "<h2>Hello, We are SEMO!</h2>"
                + "<p>SEMO 서비스를 이용해주시는 고객 여러분 감사합니다.</p>"
                + "<br>"
                + "<p>" + memberRegister.getOwnerName() + "님 안녕하세요, SEMO 서비스 이용 신청을 주셔서 감사드립니다.</p>"
                + "<p>아래의 아이디를 통해 SEMO 서비스를 정상적으로 이용 가능합니다.(초기 비밀번호:" + memberRegister.getPassword() + ")</p>"
                + "<br><br>"
                + "<a href='#' style='background-color:#64739f; color:white; padding:10px 135px; text-decoration:none; border-radius:50px; font-weight:bold;'>"
                + memberRegister.getLoginId() + "</a>"
                + "<br><br>"
                + "<p>SEMO 는 서비스를 더욱 편리하게 이용하실 수 있도록 항상 최선을 다하겠습니다.</p>"
                + "<br>"
                + "<p>감사합니다.</p>"
                + "<br><br>"
                + "<p style='font-size:10px;'>본 메일은 " + currentDate + " 기준으로 작성되었습니다.</p>"
                + "</body>"
                + "</html>";

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

        message.setContent(multipart); // 멀티파트로 설정

        try {
            // 이메일 전송
            Transport.send(message);
            // 성공 로그 출력
            log.info(">>> [ ✅ 회원가입 완료 이메일 전송 성공 - 수신자: {} ]", to);
        } catch (MessagingException e) {
            // 에러 로그 출력 및 예외 발생
            log.error(">>> [ ❌ 이메일 전송 실패: {} ]", e.getMessage());
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage(), e);
        }
    }

}

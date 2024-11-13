package sandbox.semo.application.email.service;

import static java.util.concurrent.TimeUnit.MINUTES;
import static sandbox.semo.application.email.constants.EmailConstants.AUTH_CODE_SUBJECT;
import static sandbox.semo.application.email.constants.EmailConstants.AUTH_CODE_TEMPLATE;
import static sandbox.semo.application.email.constants.EmailConstants.COMPANY_CONFIRM_SUBJECT;
import static sandbox.semo.application.email.constants.EmailConstants.COMPANY_CONFIRM_SUCCESS;
import static sandbox.semo.application.email.constants.EmailConstants.COMPANY_CONFIRM_TEMPLATE;
import static sandbox.semo.application.email.constants.EmailConstants.COMPANY_REJECT_SUBJECT;
import static sandbox.semo.application.email.constants.EmailConstants.COMPANY_REJECT_SUCCESS;
import static sandbox.semo.application.email.constants.EmailConstants.COMPANY_REJECT_TEMPLATE;
import static sandbox.semo.application.email.constants.EmailConstants.DATE_FORMAT;
import static sandbox.semo.application.email.constants.EmailConstants.MEMBER_CONFIRM_SUBJECT;
import static sandbox.semo.application.email.constants.EmailConstants.MEMBER_CONFIRM_SUCCESS;
import static sandbox.semo.application.email.constants.EmailConstants.MEMBER_CONFIRM_TEMPLATE;
import static sandbox.semo.application.email.constants.EmailConstants.MEMBER_REJECT_SUBJECT;
import static sandbox.semo.application.email.constants.EmailConstants.MEMBER_REJECT_SUCCESS;
import static sandbox.semo.application.email.constants.EmailConstants.MEMBER_REJECT_TEMPLATE;
import static sandbox.semo.application.email.constants.EmailConstants.REDIS_KEY_PREFIX;
import static sandbox.semo.application.email.exception.EmailErrorCode.APPROVAL_DENIED;
import static sandbox.semo.application.email.exception.EmailErrorCode.COMPANY_NAME_MISSING;
import static sandbox.semo.application.email.exception.EmailErrorCode.EMAIL_SEND_FAILED;
import static sandbox.semo.application.email.exception.EmailErrorCode.EMAIL_TEMPLATE_LOAD_FAILED;
import static sandbox.semo.application.email.exception.EmailErrorCode.INVALID_AUTH_CODE;
import static sandbox.semo.application.email.exception.EmailErrorCode.INVALID_REQUEST;
import static sandbox.semo.application.member.exception.MemberErrorCode.MEMBER_NOT_FOUND;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sandbox.semo.application.common.config.EmailConfig;
import sandbox.semo.application.email.exception.EmailBusinessException;
import sandbox.semo.application.member.exception.MemberBusinessException;
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

    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberRepository memberRepository;
    private final CompanyFormRepository companyFormRepository;
    private final EmailConfig config;
    private final Session mailSession;
    private final EmergencyCache emergencyCache = new EmergencyCache();

    @Override
    public void sendEmailAuthCode(String email) {
        String authCode = String.format("%06d", new Random().nextInt(999999));
        try {
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + email, authCode, 5, MINUTES);
        } catch (RedisConnectionFailureException e) {
            log.warn(">>> [ ⚠️ Redis 연결 실패 - 로컬 캐시에 인증 코드 저장 ]");
            emergencyCache.put(REDIS_KEY_PREFIX + email, authCode, 5, MINUTES);
        }
        sendMailWithTemplate(email, AUTH_CODE_SUBJECT, AUTH_CODE_TEMPLATE, Map.of("authCode", authCode));
    }

    /**
     * 이메일 템플릿 전송
     *
     * @param email        수신자의 이메일 주소
     * @param title        이메일 제목
     * @param template     이메일 템플릿 HTML 파일명
     * @param placeholders 템플릿 치환 값 (ex. {{authCode}})
     */
    private void sendMailWithTemplate(String email, String title, String template,
            Map<String, String> placeholders) {
        String htmlContent = readHtmlTemplate(template);
        htmlContent = placeholders.entrySet().stream()
                .reduce(htmlContent,
                        (content, entry) -> content.replace("{{" + entry.getKey() + "}}", entry.getValue()),
                        (content1, content2) -> content1);
        htmlContent = htmlContent.replace("{{currentDate}}", new SimpleDateFormat(DATE_FORMAT)
                .format(new Date()));
        sendMail(email, title, htmlContent);
    }

    /**
     * HTML 템플릿 파일 로드 메서드
     *
     * @param templateFileName 템플릿 파일 이름
     * @return 파일 내용을 담은 문자열
     */
    private String readHtmlTemplate(String templateFileName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + templateFileName);
            return Files.readString(resource.getFile().toPath());
        } catch (IOException e) {
            log.error(">>> [ ❌ 이메일 템플릿을 불러오는데 실패했습니다: {} ]", e.getMessage());
            throw new EmailBusinessException(EMAIL_TEMPLATE_LOAD_FAILED);
        }
    }

    private void sendMail(String emailReceive, String title, String htmlContent) {
        try {
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            attachInlineImage(multipart);

            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(config.getFrom()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailReceive));
            message.setSubject(title);
            message.setContent(multipart);

            Transport.send(message);
            log.info(">>> [ ✅ 이메일 전송 성공 - 수신자: {} ]", emailReceive);
        } catch (MessagingException | IOException e) {
            log.error(">>> [ ❌ 이메일 전송 실패 - 수신자: {} ]", emailReceive, e);
            throw new EmailBusinessException(EMAIL_SEND_FAILED);
        }
    }

    private void attachInlineImage(Multipart multipart) throws MessagingException, IOException {
        MimeBodyPart imagePart = new MimeBodyPart();
        ClassPathResource imgResource = new ClassPathResource("img/logo.png");
        imagePart.attachFile(imgResource.getFile());
        imagePart.setContentID("<blockImage>");
        imagePart.setDisposition(MimeBodyPart.INLINE);
        multipart.addBodyPart(imagePart);
    }

    /**
     * 인증 코드 검증 및 Redis Key 삭제
     *
     * @param request 이메일과 인증 코드가 포함된 요청 객체
     */
    @Override
    public void verifyEmailAuthCode(EmailAuthVerify request) {
        String key = REDIS_KEY_PREFIX + request.getEmail();
        try {
            validAuthCodeInRedis(request.getAuthCode(), key);
            redisTemplate.delete(key);
        } catch (RedisConnectionFailureException e) {
            validAuthCodeInLocalCache(request.getAuthCode(), key);
            emergencyCache.remove(key);
        }
    }

    /**
     * Redis에 저장된 인증 코드와 입력된 인증 코드를 검증
     *
     * @param target   사용자로부터 입력받은 인증 코드
     * @param redisKey Redis에 저장된 인증 코드의 키
     */
    private void validAuthCodeInRedis(String target, String redisKey) {
        String storedAuthCode = (String) redisTemplate.opsForValue().get(redisKey);
        if (storedAuthCode == null || !storedAuthCode.equals(target)) {
            log.error(">>> [ ❌ 인증 코드 불일치 - Redis 인증 코드: {}, 입력된 인증 코드: {} ]",
                    storedAuthCode,
                    target
            );
            throw new EmailBusinessException(INVALID_AUTH_CODE);
        }
        log.info(">>> [ ✅ 인증 코드 일치 - Redis ]");
    }

    /**
     * localCache에 저장된 인증 코드와 입력된 인증 코드를 검증
     *
     * @param target   사용자로부터 입력받은 인증 코드
     * @param localCacheKey localCache에 저장된 인증 코드의 키
     */
    private void validAuthCodeInLocalCache(String target, String localCacheKey) {
        log.warn(">>> [ ⚠️ Redis 연결 실패 - 로컬 캐시에서 인증 코드 확인 시도 ]");
        String storedAuthCode = emergencyCache.get(localCacheKey);
        if (storedAuthCode == null || !storedAuthCode.equals(target)) {
            log.error(">>> [ ❌ 인증 코드 불일치 - 로컬 캐시 인증 코드 없음 또는 불일치 ]");
            throw new EmailBusinessException(INVALID_AUTH_CODE);
        }
        log.info(">>> [ ✅ 인증 코드 일치 - LocalCache ]");
    }

    /**
     * 이메일 요청 처리 (등록 및 실패 알림)
     *
     * @param request 이메일 전송 요청 객체 (API 타입과 요청 값 포함)
     * @return 이메일 전송 성공 메시지
     */
    @Override
    public String processEmailRequest(EmailSendRequest request) {
        String apiType = request.getApiType();
        String value = request.getValue();
        log.info(">>> [ 📤 이메일 전송 요청 형식: {} ]", apiType);
        return switch (apiType) {
            case "REGISTER_MEMBER" -> sendMemberConfirm(value);
            case "REGISTER_COMPANY" -> sendCompanyConfirm(Long.parseLong(value));
            case "FAIL_MEMBER" -> sendMemberFail(value);
            case "FAIL_COMPANY" -> sendCompanyFail(value);
            default -> throw new EmailBusinessException(INVALID_REQUEST);
        };
    }

    private String sendMemberConfirm(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberBusinessException(MEMBER_NOT_FOUND));
        sendMailWithTemplate(member.getEmail(), MEMBER_CONFIRM_SUBJECT, MEMBER_CONFIRM_TEMPLATE,
                Map.of("ownerName", member.getOwnerName(), "loginId", member.getLoginId(),
                        "password", "0000"));
        return MEMBER_CONFIRM_SUCCESS;
    }

    private String sendCompanyConfirm(Long formId) {
        CompanyForm companyForm = companyFormRepository.findById(formId)
                .orElseThrow(() -> new EmailBusinessException(COMPANY_NAME_MISSING));
        if (companyForm.getFormStatus() != FormStatus.APPROVED) {
            throw new EmailBusinessException(APPROVAL_DENIED);
        }
        sendMailWithTemplate(companyForm.getEmail(), COMPANY_CONFIRM_SUBJECT,
                COMPANY_CONFIRM_TEMPLATE,
                Map.of("companyName", companyForm.getCompanyName(), "ownerName",
                        companyForm.getOwnerName()));
        return COMPANY_CONFIRM_SUCCESS;
    }

    private String sendMemberFail(String email) {
        sendMailWithTemplate(email, MEMBER_REJECT_SUBJECT, MEMBER_REJECT_TEMPLATE, Map.of());
        return MEMBER_REJECT_SUCCESS;
    }

    private String sendCompanyFail(String email) {
        sendMailWithTemplate(email, COMPANY_REJECT_SUBJECT, COMPANY_REJECT_TEMPLATE, Map.of());
        return COMPANY_REJECT_SUCCESS;
    }

    /**
     * Redis 복구 전략: 로컬 캐시에 있는 데이터를 Redis로 복원
     */
    @Scheduled(fixedDelayString = "${redis.restore.interval}")
    public void restoreCacheToRedis() {
        if (!emergencyCache.getAllEntries().isEmpty()) {
            try {
                emergencyCache.getAllEntries().forEach((key, value) -> {
                    redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
                    emergencyCache.remove(key);
                });
                resetSchedulerInterval(60000);
            } catch (RedisConnectionFailureException e) {
                log.warn(">>> [ ⚠️ Redis 연결 실패 - 복원 시도 중단 및 지연 간격 증가 ]");
                increaseSchedulerInterval();
            }
        }
    }

    private void resetSchedulerInterval(long interval) {
        System.setProperty("redis.restore.interval", String.valueOf(interval));
    }

    private void increaseSchedulerInterval() {
        long currentInterval = Long.parseLong(System.getProperty("redis.restore.interval", "60000"));
        long newInterval = Math.min(currentInterval * 2, 600000); // 최대 10분
        System.setProperty("redis.restore.interval", String.valueOf(newInterval));
        log.info(">>> [ ⏳ 지연 간격 증가: {} ms ]", newInterval);
    }

}

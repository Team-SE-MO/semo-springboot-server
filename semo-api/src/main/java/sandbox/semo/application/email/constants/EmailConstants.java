package sandbox.semo.application.email.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailConstants {

    /**
     * HTML Email Template File Name
     */
    public static final String AUTH_CODE_TEMPLATE = "send-auth-code.html";
    public static final String MEMBER_CONFIRM_TEMPLATE = "member-registration.html";
    public static final String COMPANY_CONFIRM_TEMPLATE = "company-registration.html";
    public static final String MEMBER_REJECT_TEMPLATE = "member-rejection.html";
    public static final String COMPANY_REJECT_TEMPLATE = "company-rejection.html";

    /**
     * Email Send Title Message
     */
    public static final String AUTH_CODE_SUBJECT = "[SEMO] 이메일 인증 코드 발송.";
    public static final String MEMBER_CONFIRM_SUBJECT = "[SEMO] 계정 등록이 완료 되었습니다.";
    public static final String COMPANY_CONFIRM_SUBJECT = "[SEMO] 회사 등록이 완료 되었습니다.";
    public static final String MEMBER_REJECT_SUBJECT = "[SEMO] 회원가입 반려 안내";
    public static final String COMPANY_REJECT_SUBJECT = "[SEMO] 회사 등록 반려 안내";

    /**
     * API Response Success Message
     */
    public static final String MEMBER_CONFIRM_SUCCESS = "성공적으로 사용자 등록 완료 이메일을 전송하였습니다.";
    public static final String COMPANY_CONFIRM_SUCCESS = "성공적으로 회사 등록 완료 이메일을 전송하였습니다.";
    public static final String MEMBER_REJECT_SUCCESS = "성공적으로 사용자 등록 반려 이메일을 전송하였습니다.";
    public static final String COMPANY_REJECT_SUCCESS = "성공적으로 회사 등록 반려 이메일을 전송하였습니다.";

    /**
     * Email Used ETC Constants
     */
    public static final String REDIS_KEY_PREFIX = "authCode:";
    public static final String DATE_FORMAT = "yyyy년 MM월 dd일 HH시 mm분";

}

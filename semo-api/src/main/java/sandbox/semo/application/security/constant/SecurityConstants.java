package sandbox.semo.application.security.constant;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityConstants {

    // JWT 관련 상수
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

    // API
    public static final String API_MAIN_PATH = "/";
    public static final String API_LOGIN_PATH = "/api/v1/login";
    public static final String API_LOGOUT_PATH = "/api/v1/logout";
    public static final String API_MAIL_AUTH_PATH = "/api/v1/mail/auth";
    public static final String API_MAIL_VALID_PATH = "/api/v1/mail/valid";
    public static final String API_CHANGE_PASSWORD_PATH = "/api/v1/member";
    public static final String API_EMAIL_CHECK = "/api/v1/member/email-check/**";
    public static final String API_MEMBER_FORM = "/api/v1/member/form";
    public static final String API_COMPANY_LIST_PATH = "/api/v1/company";
    public static final String API_COMPANY_FORM_PATH = "/api/v1/company/form";


    public static final List<String> PUBLIC_PATHS = List.of(
            API_MAIN_PATH,
            API_LOGIN_PATH,
            API_MAIL_AUTH_PATH,
            API_MAIL_VALID_PATH,
            API_CHANGE_PASSWORD_PATH,
            API_EMAIL_CHECK,
            API_MEMBER_FORM,
            API_COMPANY_LIST_PATH,
            API_COMPANY_FORM_PATH
    );

    public record JwtPathPattern(String method, String path) {
    }

    public static final List<JwtPathPattern> JWT_PATHS = List.of(
            new JwtPathPattern("*", API_MAIN_PATH),
            new JwtPathPattern("*", API_LOGIN_PATH),
            new JwtPathPattern("POST", API_MAIL_AUTH_PATH),
            new JwtPathPattern("POST", API_MAIL_VALID_PATH),
            new JwtPathPattern("GET", API_EMAIL_CHECK),
            new JwtPathPattern("POST", API_MEMBER_FORM),
            new JwtPathPattern("GET", API_COMPANY_LIST_PATH),
            new JwtPathPattern("POST", API_COMPANY_FORM_PATH),
            new JwtPathPattern("PATCH", API_CHANGE_PASSWORD_PATH)
    );

}

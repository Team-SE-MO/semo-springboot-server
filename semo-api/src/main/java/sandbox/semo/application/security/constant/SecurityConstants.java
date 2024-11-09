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
    public static final String API_MAIL_PATH = "/api/v1/mail/**";
    public static final String API_MEMBER_REGISTER_PATH = "/api/v1/member/form";
    public static final String API_COMPANY_REGISTER_PATH = "/api/v1/company/**";

    public static final List<String> PUBLIC_PATHS = List.of(
        API_MAIN_PATH,
        API_LOGIN_PATH,
        API_MAIL_PATH,
        API_MEMBER_REGISTER_PATH,
        API_COMPANY_REGISTER_PATH
    );

}

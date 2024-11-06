package sandbox.semo.application.security.authentication;

import static sandbox.semo.application.security.constant.SecurityConstants.ACCESS_CONTROL_EXPOSE_HEADERS;
import static sandbox.semo.application.security.constant.SecurityConstants.API_LOGIN_PATH;
import static sandbox.semo.application.security.constant.SecurityConstants.AUTHORIZATION_HEADER;
import static sandbox.semo.application.security.exception.AuthErrorCode.UNAUTHORIZED_USER;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sandbox.semo.application.security.exception.AuthErrorCode;
import sandbox.semo.application.security.util.JsonResponseHelper;
import sandbox.semo.application.security.util.JwtUtil;


@Log4j2
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl(API_LOGIN_PATH);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
        HttpServletResponse response) throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, FilterChain chain, Authentication authentication)
        throws IOException, ServletException {
        LoginMemberDetails loginMemberDetails = (LoginMemberDetails) authentication.getPrincipal();

        Long memberId = loginMemberDetails.getMemberId();
        String username = loginMemberDetails.getUsername();
        String role = authentication.getAuthorities().stream()
            .findFirst()
            .map(GrantedAuthority::getAuthority)
            .orElseThrow(() -> new BadCredentialsException(UNAUTHORIZED_USER.getMessage()));
        Long companyId = loginMemberDetails.getCompanyId();
        String token = jwtUtil.generateToken(memberId, username, role, companyId);

        response.addHeader(ACCESS_CONTROL_EXPOSE_HEADERS, AUTHORIZATION_HEADER);
        response.addHeader(AUTHORIZATION_HEADER, token);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("code", 200);
        responseBody.put("message", "사용자 인증이 완료 되어 로그인 되었습니다.");
        responseBody.put("data", Map.of("ROLE", role));

        JsonResponseHelper.sendJsonSuccessResponse(response, responseBody);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
        HttpServletResponse response, AuthenticationException exception)
        throws IOException, ServletException {
        log.error(">>> [ ❌ 인증 실패: {} ]", exception.getMessage());
        AuthErrorCode authErrorCode = AuthErrorCode.fromAuthenticationException(exception);
        JsonResponseHelper.sendJsonErrorResponse(response, authErrorCode);
    }

}

package sandbox.semo.application.security.authentication;

import static sandbox.semo.application.security.constant.SecurityConstants.API_LOGIN_PATH;
import static sandbox.semo.application.security.exception.AuthErrorCode.BLACKLISTED_TOKEN;
import static sandbox.semo.application.security.exception.AuthErrorCode.INVALID_AUTH_REQUEST;
import static sandbox.semo.application.security.exception.AuthErrorCode.INVALID_TOKEN;
import static sandbox.semo.application.security.exception.AuthErrorCode.TOKEN_EXPIRED;
import static sandbox.semo.application.security.exception.AuthErrorCode.UNAUTHORIZED_USER;
import static sandbox.semo.application.security.util.TokenExtractor.extractToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sandbox.semo.application.security.exception.AuthErrorCode;
import sandbox.semo.application.security.util.JsonResponseHelper;
import sandbox.semo.application.security.util.JwtUtil;
import sandbox.semo.application.security.util.RedisUtil;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().equals(API_LOGIN_PATH)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        String token = extractToken(authorization);
        try {
            if (redisUtil.isBlacklisted(token)) {
                log.error(">>> [ ❌ 로그아웃된 토큰입니다 ]");
                handleAuthenticationException(response, BLACKLISTED_TOKEN);
                return;
            }
            processAuthentication(token);
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error(">>> [ ❌ 토큰이 만료되었습니다 ]");
            handleAuthenticationException(response, TOKEN_EXPIRED);
            return;
        } catch (BadCredentialsException e) {
            log.error(">>> [ ❌ 인증 실패: {} ]", e.getMessage());
            handleAuthenticationException(response, INVALID_AUTH_REQUEST);
        } catch (JwtException e) {
            log.error(">>> [ ❌ 유효하지 않은 토큰입니다: {} ]", e.getMessage());
            handleAuthenticationException(response, INVALID_TOKEN);
            return;
        } catch (Exception e) {
            log.error(">>> [ ❌ 인증 처리 중 오류 발생: {} ]", e.getMessage());
            handleAuthenticationException(response, UNAUTHORIZED_USER);
            return;
        }
    }

    private void processAuthentication(String token) {
        Claims claims = jwtUtil.validateAndGetClaimsFromToken(token);

        JwtMemberDetails principalDetails = new JwtMemberDetails(
            jwtUtil.getMemberId(claims),
            jwtUtil.getCompanyId(claims),
            jwtUtil.getRole(claims),
            jwtUtil.getLoginId(claims)
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principalDetails,
            null,
            principalDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleAuthenticationException(HttpServletResponse response,
        AuthErrorCode errorCode) throws IOException {
        SecurityContextHolder.clearContext();
        JsonResponseHelper.sendJsonErrorResponse(response, errorCode);
    }
}

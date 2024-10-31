package sandbox.semo.application.security.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sandbox.semo.application.security.exception.AuthErrorCode;
import sandbox.semo.application.security.util.JsonResponseHelper;
import sandbox.semo.application.security.util.JwtUtil;

import java.io.IOException;

import static sandbox.semo.application.security.constant.SecurityConstants.JWT_TOKEN_PREFIX;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                processAuthentication(authorization);
            } catch (ExpiredJwtException e) {
                log.error(">>> [ ❌ 토큰이 만료되었습니다 ]");
                handleAuthenticationException(response, AuthErrorCode.TOKEN_EXPIRED);
                return;
            } catch (JwtException e) {
                log.error(">>> [ ❌ 유효하지 않은 토큰입니다: {} ]", e.getMessage());
                handleAuthenticationException(response, AuthErrorCode.INVALID_TOKEN);
                return;
            } catch (Exception e) {
                log.error(">>> [ ❌ 인증 처리 중 오류 발생: {} ]", e.getMessage());
                handleAuthenticationException(response, AuthErrorCode.UNAUTHORIZED_USER);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void processAuthentication(String authorization) {
        String token = authorization.substring(JWT_TOKEN_PREFIX.length());
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

    private void handleAuthenticationException(HttpServletResponse response, AuthErrorCode errorCode) throws IOException {
        SecurityContextHolder.clearContext();
        JsonResponseHelper.sendJsonErrorResponse(response, errorCode);
    }
}

package sandbox.semo.application.security.authentication;

import static sandbox.semo.application.security.constant.SecurityConstants.JWT_TOKEN_PREFIX;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sandbox.semo.application.security.exception.AuthErrorCode;
import sandbox.semo.application.security.util.JsonResponseHelper;
import sandbox.semo.application.security.util.JwtUtil;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberPrincipalDetailService memberDetailService; 

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        log.info("Authorization 헤더: [{}]", authorization);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.info("토큰 정보가 존재하지 않습니다");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(JWT_TOKEN_PREFIX.length());
        System.out.println(token);

        //토큰 소멸 검증
        if (jwtUtil.isTokenExpired(token)) {
            log.error(">>> [ ❌ 토큰이 만료되었습니다. ]");
            JsonResponseHelper.sendJsonErrorResponse(response, AuthErrorCode.TOKEN_EXPIRED);
            return;
        }

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
//        Long companyId = jwtUtil.getCompanyId(token);

        MemberPrincipalDetails principalDetails =
                (MemberPrincipalDetails) memberDetailService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principalDetails,  // Principal로 MemberPrincipalDetails 사용
                        null,
                        principalDetails.getAuthorities()  // Member의 실제 권한 정보 사용
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}

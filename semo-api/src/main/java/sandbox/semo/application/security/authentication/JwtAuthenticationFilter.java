package sandbox.semo.application.security.authentication;

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
    private final MemberPrincipalDetailService memberDetailService;  // 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {

            log.info("토큰 정보가 존재하지 않습니다");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("authorization now");
        //Bearer 부분 제거 후 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        //토큰 소멸 검증
        if (jwtUtil.isTokenExpired(token)) {
            log.error(">>> [ ❌ 토큰이 만료되었습니다. ]");
            JsonResponseHelper.sendJsonErrorResponse(response, AuthErrorCode.TOKEN_EXPIRED);
            return;
        }

        String username = jwtUtil.getUsername(token);

        MemberPrincipalDetails userDetails =
                (MemberPrincipalDetails) memberDetailService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,  // Principal로 MemberPrincipalDetails 사용
                        null,
                        userDetails.getAuthorities()  // Member의 실제 권한 정보 사용
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}

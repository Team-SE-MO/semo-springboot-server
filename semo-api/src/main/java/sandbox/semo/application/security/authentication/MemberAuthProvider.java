package sandbox.semo.application.security.authentication;

import static sandbox.semo.application.security.exception.AuthErrorCode.INVALID_CREDENTIALS;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MemberAuthProvider implements AuthenticationProvider {

    private final LoginMemberDetailService loginMemberDetailService;

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        String loginId = authentication.getName();
        String password = authentication.getCredentials().toString();

        log.info(">>> [ 🚀 로그인 시도 - 아이디: {} ]", loginId);
        LoginMemberDetails loginMemberDetails =
            (LoginMemberDetails) loginMemberDetailService.loadUserByUsername(loginId);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(password, loginMemberDetails.getPassword())) {
            throw new BadCredentialsException(INVALID_CREDENTIALS.getMessage());
        }

        log.info(">>> [ ✅ 사용자 인증이 완료 되었습니다. ]");
        return new UsernamePasswordAuthenticationToken(
            loginMemberDetails, null, loginMemberDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}

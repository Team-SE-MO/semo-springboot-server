package sandbox.semo.application.security.authentication;

import static sandbox.semo.application.security.exception.AuthErrorCode.INVALID_CREDENTIALS;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class LoginMemberDetailService implements UserDetailsService {

    private final MemberRepository repository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = repository.findByLoginIdAndDeletedAtIsNull(loginId)
                .orElseThrow(() -> new UsernameNotFoundException(INVALID_CREDENTIALS.getMessage()));
        return new LoginMemberDetails(member);
    }

}

package sandbox.semo.security.authentication;

import static sandbox.semo.security.exception.ErrorCode.INVALID_CREDENTIALS;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sandbox.semo.member.entity.Member;
import sandbox.semo.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberPrincipalDetailService implements UserDetailsService {

    private final MemberRepository repository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        Member member = repository.findByLoginIdAndDeletedAtIsNull(loginId)
                .orElseThrow(() -> new UsernameNotFoundException(INVALID_CREDENTIALS.getMessage()));
        return new MemberPrincipalDetails(member);
    }

}

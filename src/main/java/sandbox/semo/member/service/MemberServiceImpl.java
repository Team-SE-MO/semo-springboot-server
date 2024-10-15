package sandbox.semo.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sandbox.semo.member.dto.request.MemberRegister;
import sandbox.semo.company.entity.Company;
import sandbox.semo.member.entity.Member;
import sandbox.semo.member.entity.Role;
import sandbox.semo.company.repository.CompanyRepository;
import sandbox.semo.member.repository.MemberRepository;

// TODO: Security 설정 확인 용도로 가볍게 구현 했음. Member API 개발시 추가 코드 필요.
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(MemberRegister request) {
        Member member = Member.builder()
                .company(getCompanyById(request.getCompanyId()))
                .ownerName(request.getOwnerName())
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .build();
        memberRepository.save(member);
    }

    private Company getCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }

}

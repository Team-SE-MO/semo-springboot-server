package sandbox.semo.domain.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sandbox.semo.domain.member.dto.response.MemberRegister;
import sandbox.semo.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);
    Optional<Member> findByLoginIdAndDeletedAtIsNull(String loginId);

    @Query()
    MemberRegister findByEmail(String email);
}

package sandbox.semo.domain.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByLoginIdAndDeletedAtIsNull(String loginId);

    Boolean existsByEmail(String email);

}

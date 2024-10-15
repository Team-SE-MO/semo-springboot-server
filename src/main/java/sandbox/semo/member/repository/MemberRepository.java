package sandbox.semo.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);
    Optional<Member> findByLoginIdAndDeletedAtIsNull(String loginId);

}

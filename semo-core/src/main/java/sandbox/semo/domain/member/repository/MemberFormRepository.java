package sandbox.semo.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.domain.member.entity.MemberForm;

public interface MemberFormRepository extends JpaRepository<MemberForm, Long> {

}

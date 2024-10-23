package sandbox.semo.domain.form.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.domain.form.entity.MemberForm;

public interface MemberFormRepository extends JpaRepository<MemberForm, Long> {
}
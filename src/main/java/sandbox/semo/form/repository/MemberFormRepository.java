package sandbox.semo.form.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.form.entity.MemberForm;

public interface MemberFormRepository extends JpaRepository<MemberForm, Long> {


}

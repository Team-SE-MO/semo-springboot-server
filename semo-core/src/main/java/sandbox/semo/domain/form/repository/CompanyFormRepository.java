package sandbox.semo.domain.form.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.domain.form.entity.CompanyForm;

public interface CompanyFormRepository extends JpaRepository<CompanyForm, Long> {
}

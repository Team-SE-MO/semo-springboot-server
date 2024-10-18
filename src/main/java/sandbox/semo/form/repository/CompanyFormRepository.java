package sandbox.semo.form.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.form.entity.CompanyForm;

public interface CompanyFormRepository extends JpaRepository<CompanyForm, Long> {

}

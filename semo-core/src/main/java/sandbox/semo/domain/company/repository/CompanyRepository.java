package sandbox.semo.domain.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.domain.company.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

}

package sandbox.semo.company.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sandbox.semo.company.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

}

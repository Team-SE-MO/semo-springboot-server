package sandbox.semo.domain.company.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sandbox.semo.domain.company.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByTaxId(String taxId);

    @Query("SELECT c FROM Company c WHERE (:keyword IS NULL OR"
            + " c.companyName LIKE CONCAT('%', :keyword, '%')) AND c.id != 1")
    List<Company> findAllContainsKeywords(@Param("keyword") String keyword);

    @Query(value = """
                SELECT c FROM Company c
                WHERE TRIM(LOWER(c.companyName)) = TRIM(LOWER(:companyName))
                  AND TRIM(c.taxId) = TRIM(:taxId)
            """)
    Optional<Company> findByCompanyNameAndTaxId(
            @Param("companyName") String companyName,
            @Param("taxId") String taxId
    );

}

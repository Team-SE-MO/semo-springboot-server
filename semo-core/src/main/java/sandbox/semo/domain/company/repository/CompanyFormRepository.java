package sandbox.semo.domain.company.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sandbox.semo.domain.company.entity.CompanyForm;

public interface CompanyFormRepository extends JpaRepository<CompanyForm, Long> {

    @Query(value = """
            SELECT * FROM COMPANY_FORM
            ORDER BY REQUEST_DATE DESC
            OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
            """, nativeQuery = true)
    List<CompanyForm> findPageWithOffset(@Param("offset") int offset, @Param("size") int size);

}

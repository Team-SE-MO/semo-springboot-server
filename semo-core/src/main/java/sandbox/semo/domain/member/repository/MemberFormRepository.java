package sandbox.semo.domain.member.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sandbox.semo.domain.member.entity.MemberForm;

public interface MemberFormRepository extends JpaRepository<MemberForm, Long> {

    @Query(value = """
            SELECT * FROM MEMBER_FORM
            ORDER BY REQUEST_DATE DESC
            OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
            """, nativeQuery = true)
    List<MemberForm> findPageWithOffset(@Param("offset") int offset, @Param("size") int size);

}

package sandbox.semo.domain.member.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sandbox.semo.domain.member.entity.MemberForm;

public interface MemberFormRepository extends JpaRepository<MemberForm, Long> {

    @Query(value = """
            SELECT * FROM MEMBER_FORM 
            ORDER BY FORM_ID DESC 
            FETCH FIRST :size ROWS ONLY
            """, nativeQuery = true)
    List<MemberForm> findFirstPage(@Param("size") int size);

    @Query(value = """
            SELECT * FROM MEMBER_FORM 
            WHERE FORM_ID < :cursor 
            ORDER BY FORM_ID DESC 
            FETCH FIRST :size ROWS ONLY
            """, nativeQuery = true)
    List<MemberForm> findNextPage(@Param("cursor") Long cursor, @Param("size") int size);

}

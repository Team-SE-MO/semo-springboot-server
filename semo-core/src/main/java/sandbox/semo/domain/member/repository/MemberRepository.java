package sandbox.semo.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sandbox.semo.domain.member.dto.response.MemberInfo;
import sandbox.semo.domain.member.entity.Member;
import sandbox.semo.domain.member.entity.Role;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByIdAndDeletedAtIsNull(Long id);

    Optional<Member> findByLoginId(String loginId);

    Optional<Member> findByLoginIdAndDeletedAtIsNull(String loginId);

    Optional<Member> findByEmailAndDeletedAtIsNull(String email);

    Optional<Member> findByEmail(String email);

    @Query(value = """
        SELECT new sandbox.semo.domain.member.dto.response.MemberInfo
            (m.loginId, m.role, m.email, m.ownerName, m.deletedAt, m.company)
        FROM Member m
        JOIN m.company c
        WHERE (:ownRole != 'ROLE_SUPER' AND c.id = :ownCompanyId OR :ownRole = 'ROLE_SUPER')
          AND (:companyId IS NULL OR c.id = :companyId)
          AND (COALESCE(:roles, NULL) IS NULL OR m.role IN (:roles))
          AND (:keyword IS NULL OR :keyword = ''
               OR m.loginId LIKE CONCAT('%', :keyword, '%')
               OR m.ownerName LIKE CONCAT('%', :keyword, '%')
               OR m.email LIKE CONCAT('%', :keyword, '%'))
          AND m.deletedAt IS NULL
        ORDER BY m.updatedAt DESC
        OFFSET :offset ROWS FETCH NEXT :size ROWS ONLY
    """)
    List<MemberInfo> findAllActiveMemberContainsRoleWithPagination(
            @Param("ownRole") String ownRole,
            @Param("ownCompanyId") Long ownCompanyId,
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword,
            @Param("roles") List<Role> roles,
            @Param("offset") int offset,
            @Param("size") int size
    );

    @Query(value = """
        SELECT COUNT(m)
        FROM Member m
        JOIN m.company c
        WHERE (:ownRole != 'ROLE_SUPER' AND c.id = :ownCompanyId OR :ownRole = 'ROLE_SUPER')
          AND (:companyId IS NULL OR c.id = :companyId)
          AND (COALESCE(:roles, NULL) IS NULL OR m.role IN (:roles))
          AND (:keyword IS NULL OR :keyword = ''
               OR m.loginId LIKE CONCAT('%', :keyword, '%')
               OR m.ownerName LIKE CONCAT('%', :keyword, '%')
               OR m.email LIKE CONCAT('%', :keyword, '%'))
          AND m.deletedAt IS NULL
    """)
    long countAllActiveMemberContainsRole(
            @Param("ownRole") String ownRole,
            @Param("ownCompanyId") Long ownCompanyId,
            @Param("companyId") Long companyId,
            @Param("keyword") String keyword,
            @Param("roles") List<Role> roles
    );


}

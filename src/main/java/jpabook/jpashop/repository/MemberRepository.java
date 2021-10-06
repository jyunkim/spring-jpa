package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.dto.MemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByName(String name); // 컬렉션

    Member findMemberByName(String name); // 단건

    Optional<Member> findOptionalByName(String name); // 단건 Optional

    @Query("select m from Member m where m.name = :name and m.age = :age")
    List<Member> findUser(@Param("name") String name, @Param("age") int age);

    @Query("select m.name from Member m")
    List<String> findNameList();

    @Query("select new jpabook.jpashop.dto.MemberDto(m.id, m.name) from Member m")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.name in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    // 조인 사용 시 count 쿼리를 별도로 분리하여 성능 최적화
    @Query(
            value = "select m from Member m left join m.team",
            countQuery = "select count(m) from Member m"
    )
    Page<Member> findPageByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);

    List<Member> findMemberByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByName(String name);

    List<NameOnly> findProjectionsByName(String name);

    @Query(value = "select * from member where name = ?", nativeQuery = true)
    List<Member> findByNativeQuery(String name);

    @Query(
            value = "select m.name from member as m",
            countQuery = "select count(*) from member",
            nativeQuery = true
    )
    Page<NameOnly> findByNativeProjection(Pageable pageable);
}

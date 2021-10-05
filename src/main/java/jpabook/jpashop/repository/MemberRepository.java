package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.dto.MemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

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
}

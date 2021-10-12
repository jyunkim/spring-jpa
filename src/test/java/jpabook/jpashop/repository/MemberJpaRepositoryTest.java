package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Team;
import jpabook.jpashop.dto.MemberSearchCondition;
import jpabook.jpashop.dto.MemberTeamDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class) // JUnit4에는 필요
@SpringBootTest
@Transactional // JPA를 통한 모든 데이터 변경은 트랜잭션 안에서 실행해야 함
// @Commit -> DB에 트랜잭션 커밋. 디폴트 값은 롤백
public class MemberJpaRepositoryTest {

    @Autowired MemberJpaRepository memberJpaRepository;
    @Autowired TeamRepository teamRepository;

    @Test // JUnit4 - org.junit.Test -> 메소드는 반드시 public이어야 함
    public void testMember() {
        Member member = new Member();
        member.setName("memberA");

        memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.findById(1L);

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember).isEqualTo(member); // JPA 엔티티 동일성 보장
    }

    @Test
    public void paging() {
        Member m1 = new Member();
        m1.setName("member1");
        memberJpaRepository.save(m1);
        Member m2 = new Member();
        m2.setName("member2");
        memberJpaRepository.save(m2);
        Member m3 = new Member();
        m3.setName("member3");
        memberJpaRepository.save(m3);
        Member m4 = new Member();
        m4.setName("member4");
        memberJpaRepository.save(m4);
        Member m5 = new Member();
        m5.setName("member5");
        memberJpaRepository.save(m5);

        List<Member> members = memberJpaRepository.findByPage(0, 3);
        long count = memberJpaRepository.count();

        assertThat(members.size()).isEqualTo(3);
        assertThat(count).isEqualTo(5);
    }

    @Test
    public void basicQuerydsl() {
        Member member = new Member();
        member.setName("member1");
        member.setAge(10);
        memberJpaRepository.save(member);

        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByName_Querydsl("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest() {
        Team t1 = new Team();
        t1.setName("team1");
        teamRepository.save(t1);

        Team t2 = new Team();
        t2.setName("team2");
        teamRepository.save(t2);

        Member m1 = new Member();
        m1.setName("member1");
        m1.setAge(10);
        m1.setTeam(t1);
        memberJpaRepository.save(m1);

        Member m2 = new Member();
        m2.setName("member2");
        m2.setAge(20);
        m2.setTeam(t2);
        memberJpaRepository.save(m2);

        Member m3 = new Member();
        m3.setName("member3");
        m3.setAge(20);
        m3.setTeam(t1);
        memberJpaRepository.save(m3);

        Member m4 = new Member();
        m4.setAge(25);
        m4.setTeam(t1);
        memberJpaRepository.save(m4);

        Member m5 = new Member();
        m5.setName("member5");
        m5.setAge(25);
        m5.setTeam(t2);
        memberJpaRepository.save(m5);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamName("team1");
        condition.setAgeGoe(20);
        condition.setAgeLoe(24);

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
        assertThat(result).extracting("username").containsExactly("member3");

        List<MemberTeamDto> result2 = memberJpaRepository.search(condition);
        assertThat(result2).extracting("username").containsExactly("member3");
    }
}

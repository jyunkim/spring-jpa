package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
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

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test // JUnit4 - org.junit.Test
    public void testMember() {
        Member member = new Member();
        member.setName("memberA");

        memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.findOne(1L);

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
}

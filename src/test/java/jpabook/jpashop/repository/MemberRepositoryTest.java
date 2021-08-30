package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class) // JUnit4에는 필요
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @Test // JUnit4 - org.junit.Test
    @Transactional // JPA를 통한 모든 데이터 변경은 트랜잭션 안에서 실행해야 함
//    @Commit -> DB에 트랜잭션 커밋. 디폴트 값은 롤백
    public void testMember() {
        Member member = new Member();
        member.setName("memberA");

        memberRepository.save(member);
        Member findMember = memberRepository.findOne(1L);

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember).isEqualTo(member); // JPA 엔티티 동일성 보장
    }
}
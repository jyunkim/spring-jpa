package jpabook.jpashop.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
public class MemberDomainTest {

    @Autowired EntityManager em;

    @Test
    void fetchJoinTest() {
        Team t1 = new Team();
        t1.setName("팀1");
        em.persist(t1);

        Team t2 = new Team();
        t2.setName("팀2");
        em.persist(t2);

        Member m1 = new Member();
        m1.setName("회원1");
        m1.setTeam(t1);
        em.persist(m1);

        Member m2 = new Member();
        m2.setName("회원2");
        m2.setTeam(t1);
        em.persist(m2);

        Member m3 = new Member();
        m3.setName("회원3");
        m3.setTeam(t2);
        em.persist(m3);

        em.flush();
        em.clear();

//        String query = "select m, t from Member m join m.team t";
////        String query = "select m from Member m where m.name='회원1'";
//
//        List<Object[]> result = em.createQuery(query)
//                .getResultList();
//
//        for (Object[] o: result) {
//            Member member = (Member) o[0];
//            System.out.println(member.getTeam().getName());
//        }

        String query = "select m from Member m join fetch m.team";
        List<Member> result = em.createQuery(query, Member.class)
                .getResultList();

    }
}

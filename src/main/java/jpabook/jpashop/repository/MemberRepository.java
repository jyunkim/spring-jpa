package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class MemberRepository {

    // 스프링 부트에서 자동 주입
    @PersistenceContext
    private EntityManager em;

    public void save(Member member) {
        // 영속성 컨텍스트에 Member 객체 삽입(PK 생성)
        // 트랜잭션이 commit되는 시점에 DB에 반영
        em.persist(member);
    }

    // Primary key로 객체 찾음
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        // JPQL - 엔티티를 대상으로 쿼리 수행
        // 두번째 인자 = 반환 클래스
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String name) {
        // JPQL 내 변수 - :~ 이름 설정 후 setParameter
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}

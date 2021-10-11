package jpabook.jpashop;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.QMember;
import jpabook.jpashop.domain.QTeam;
import jpabook.jpashop.domain.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static jpabook.jpashop.domain.QMember.*;
import static jpabook.jpashop.domain.QTeam.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);

        Team t1 = new Team();
        t1.setName("team1");
        em.persist(t1);

        Team t2 = new Team();
        t2.setName("team2");
        em.persist(t2);

        Member m1 = new Member();
        m1.setName("member1");
        m1.setAge(10);
        m1.setTeam(t1);
        em.persist(m1);

        Member m2 = new Member();
        m2.setName("member2");
        m2.setAge(20);
        m2.setTeam(t2);
        em.persist(m2);

        Member m3 = new Member();
        m3.setName("member3");
        m3.setAge(20);
        m3.setTeam(t1);
        em.persist(m3);

        Member m4 = new Member();
        m4.setAge(25);
        m4.setTeam(t1);
        em.persist(m4);

        Member m5 = new Member();
        m5.setName("member5");
        m5.setAge(25);
        m5.setTeam(t2);
        em.persist(m5);
    }

    @Test
    void startJpql() {
        String qlString = "select m from Member m where m.name = :name";
        Member member = em.createQuery(qlString, Member.class)
                .setParameter("name", "member1")
                .getSingleResult();

        assertThat(member.getName()).isEqualTo("member1");
    }

    @Test
    void startQuerydsl() {
//        QMember m = new QMember("m");
//        QMember m = QMember.member;

        // static import 사용
        Member result = queryFactory
//                .select(member)
//                .from(member)
                .selectFrom(member)
                .where(member.name.eq("member1"))
                .fetchOne();

        assertThat(result.getName()).isEqualTo("member1");
    }

    @Test
    void search() {
        Member result = queryFactory
                .selectFrom(member)
//                .where(member.name.eq("member1")
//                        .and(member.age.eq(10)))
                .where(member.name.eq("member1"),
                        member.age.eq(10))
                .fetchOne();

        assertThat(result.getName()).isEqualTo("member1");
    }

    @Test
    void paging() {
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .orderBy(member.name.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(results.getTotal()).isEqualTo(5);
        assertThat(results.getLimit()).isEqualTo(2);
        assertThat(results.getOffset()).isEqualTo(1);

        List<Member> content = results.getResults();
        assertThat(content.size()).isEqualTo(2);
        for (Member m : content) {
            System.out.println(m.getName());
        }
    }

    /**
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 오름차순 (단, 이름이 없으면 마지막에 출력)
     */
    @Test
    void sort() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.name.asc().nullsLast())
                .fetch();

        for (Member m : result) {
            System.out.println("name = " + m.getName() + " age = " + m.getAge());
        }
    }

    @Test
    void aggregation() {
        Tuple tuple = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetchOne();

        assertThat(tuple.get(member.count())).isEqualTo(5);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(20);
        assertThat(tuple.get(member.age.max())).isEqualTo(25);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple team1 = result.get(0);
        Tuple team2 = result.get(1);

        assertThat(team1.get(team.name)).isEqualTo("team1");
        assertThat(team2.get(team.name)).isEqualTo("team2");
        assertThat(team2.get(member.age.avg())).isEqualTo(22.5);
    }

    @Test
    void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("team2"))
                .fetch();

        assertThat(result)
                .extracting("name")
                .containsExactly("member2", "member5");
    }

    @Test
    void join_on() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("team1"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void theta_join() {
        Member m1 = new Member();
        m1.setName("team1");
        em.persist(m1);

        Member m2 = new Member();
        m2.setName("team2");
        em.persist(m2);

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.name.eq(team.name))
                .fetch();

        for (Member m : result) {
            System.out.println("m = " + m);
        }
    }

    @Test
    void join_on_no_relation() {
        Member m1 = new Member();
        m1.setName("team1");
        em.persist(m1);

        Member m2 = new Member();
        m2.setName("team2");
        em.persist(m2);

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.name.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void fetchJoin() {
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.name.eq("member1"))
                .fetchOne();

        System.out.println(findMember);
        System.out.println(findMember.getTeam());
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    void subQuery() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .contains(25);
    }

    @Test
    void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println(s);
        }
    }

    @Test
    void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(10, 19)).then("10대")
                        .when(member.age.between(20, 29)).then("20대")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println(s);
        }
    }

    @Test
    void concat() {
        List<String> result = queryFactory
                .select(member.name.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println(s);
        }
    }
}

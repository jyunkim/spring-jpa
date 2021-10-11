# Querydsl
## 설정
### Q타입 생성
Gradle - Tasks - other - compileQuerydsl   
-> build - generated - querydsl

## 소개
- Type-safe한 쿼리를 위한 domain specific language
- 복잡한 쿼리, 동적 쿼리 쉽게 생성 가능
- 쿼리를 자바 코드로 작성   
-> 문법 오류를 컴파일 시점에 알 수 있음
- JPQL 빌더 역할

## 기본 문법
### JPQL과의 차이
```java
@Test
void startQuerydsl() {
    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    QMember m = new QMember("m");

    Member member = queryFactory
//            .select(member)
//            .from(member)
            .selectFrom(member)
            .where(m.name.eq("member1"))
            .fetchOne();

    assertThat(member.getName()).isEqualTo("member1");
}
```
- JPQL - 문자(실행 시점 오류), 직접 파라미터 바인딩
- Querydsl - 코드(컴파일 시점 오류) , 자동 파라미터 바인딩

**JPAQueryFactory를 필드로 제공하면 동시성 문제는 어떻게 될까?**   
동시성 문제는 JPAQueryFactory를 생성할 때 제공하는 EntityManager(em)에 달려있음. 
스프링 프레임워크는 여러 쓰레드에서 동시에 같은 EntityManager에 접근해도, 트랜잭션 마다 별도의 영속성 컨텍스트를 제공하기 때문에, 
동시성 문제는 걱정하지 않아도 됨

### Q-Type
Q클래스 인스턴스 사용 방법
```java
QMember qMember = new QMember("m");
QMember qMember = QMember.member;
```
이 두 방법 외에 static import를 사용하면 변수를 선언하지 않고 사용 가능

### 검색 조건 쿼리
```java
member.username.eq("member1") // username = 'member1'
member.username.ne("member1") // username != 'member1'
member.username.eq("member1").not() // username != 'member1'
member.username.isNotNull() // 이름이 is not null
        
member.age.in(10, 20) // age in (10,20)
member.age.notIn(10, 20) // age not in (10, 20)
member.age.between(10,30) // between 10, 30
        
member.age.goe(30) // age >= 30
member.age.gt(30) // age > 30
member.age.loe(30) // age <= 30
member.age.lt(30) // age < 30
        
member.username.like("member%") // like 검색
member.username.contains("member") // like ‘%member%’ 검색
member.username.startsWith("member") // like ‘member%’ 검색
```
where()의 and 조건은 ,로 대체 가능

### 결과 조회
- fetch(): 리스트 조회, 데이터가 없으면 빈 리스트 반환
- fetchOne(): 단 건 조회
  - 결과가 없으면: null
  - 결과가 둘 이상이면: com.querydsl.core.NonUniqueResultException
- fetchFirst(): limit(1).fetchOne()
- fetchResults(): 페이징 정보 포함, total count 쿼리 추가 실행
  - 성능 최적화가 필요한 경우 count 쿼리를 별도로 만들어야 함
- fetchCount(): count 쿼리로 수 조회

### 페이징
```java
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
```

### 집합
```java
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
```
select절에 여러 데이터 타입이 있을 경우 tuple 사용

### 조인
join(조인 대상, 별칭으로 사용할 Q타입)
```java
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
```

**ON을 활용한 조인**
1. 조인 대상 필터링   
   -> 내부 조인의 경우 where와 결과 동일   
   => 외부 조인에서 필요한 경우에만 사용
2. 연관관계 없는 엔티티 외부 조인
```java
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
```
-> 실제 SQL ON절에 member.team = team.id가 적용되지 않음

**세타 조인**   
연관관계가 없는 필드로 조인
```java
@Test
void theta_join() {
    em.persist(new Member("teamA"));
    em.persist(new Member("teamB"));
    
    List<Member> result = queryFactory
        .select(member)
        .from(member, team)
        .where(member.username.eq(team.name))
        .fetch();
 
    assertThat(result)
        .extracting("username")
        .containsExactly("teamA", "teamB");
}
```

**페치 조인**   
연관된 엔티티를 SQL 한번에 조회 -> 성능 최적화
```java
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
```

### 서브 쿼리
com.querydsl.jpa.JPAExpressions 사용
```java
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
```
서브 쿼리 사용 시 별도의 alias를 사용해야 하므로 새로운 Q클래스 인스턴스 생성

from절의 서브 쿼리는 지원x   
-> 대안
1. 서브쿼리를 join으로 변경
2. 애플리케이션에 쿼리를 2번 분리해서 실행
3. 네이티브SQL 사용

### Case 문
단순한 조건
```java
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
```

복잡한 조건
```java
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
```

### 상수, 문자 더하기
상수 더하기
```java
@Test
void concat() {
    List<String> result = queryFactory
            .select(member.name, Expressions.constant("A"))
            .from(member)
            .fetch();

    for (String s : result) {
        System.out.println(s);
    }
}
```

문자 더하기
```java
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
```
stringValue(): 문자가 아닌 다른 타입을 문자로 변환   
-> Enum 타입 처리 시 자주 사용

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
}
```
stringValue(): 문자가 아닌 다른 타입을 문자로 변환   
-> Enum 타입 처리 시 자주 사용

## 프로젝션 결과 반환
- 프로젝션 대상이 하나면 타입을 명확하게 지정할 수 있음
- 프로젝션 대상이 둘 이상이면 Tuple이나 DTO로 조회   
-> 튜플은 되도록 레포지토리 계층에서만 사용. 다른 계층에서 querydsl에 대한 의존이 없게 하는게 좋음   
-> 다른 계층에서 사용할 땐 DTO로 변환해서 전송

JPQL에서 DTO를 조회할 때는 패키지 이름을 다 작성해야 함   
-> querydsl 사용

### Querydsl 빈 생성
결과를 DTO로 반환할 때 사용
- 프로퍼티 접근   
기본 생성자, setter 필요
```java
select(Projections.bean(MemberDto.class, member.name, member.age))
```
- 필드 접근   
기본 생성자 필요
```java
select(Projections.fields(MemberDto.class, member.name, member.age))
```
- 생성자 접근   
해당 타입의 파라미터를 가진 생성자 필요
```java
select(Projections.constructor(MemberDto.class, member.name, member.age))
```

프로퍼티, 필드 접근 방식에서 이름이 다른 DTO를 사용할 경우 해결 방안(Ex. username)
- member.name.as("username")   
-> 생성자 접근 방식은 타입만 같으면 되기 때문에 이름은 상관x

서브 쿼리 사용 시 별칭 적용
- ExpressionUtils.as(JPAExpressions.select(memberSub.age.max()).from(memberSub), "age")

### @QueryProjection
```java
@QueryProjection
public MemberDto(String name, int age) {
    this.name = name;
    this.age = age;
}
```
그리고 다시 compileQuerydsl -> DTO Q클래스 생성

```java
select(new QMemberDto(member.name, member.age))
```

**장점**   
- 생성자 접근 - 잘못된 인자를 넣었을 때 실행 시점에 런타임 에러로 알 수 있음
- @QueryProjection - 잘못된 인자를 넣었을 때 컴파일 에러 발생

**단점**   
DTO가 Querydsl에 대해 의존성을 가짐   
-> DTO는 여러 계층에서 사용되기 때문에 좋지 않음

## 동적 쿼리
### BooleanBuilder
```java
private List<Member> searchMember1(String nameCond, Integer ageCond) {
    // 필수값은 생성자 인자로 조건을 넣어줌
    BooleanBuilder builder = new BooleanBuilder();

    if (nameCond != null) {
        builder.and(member.name.eq(nameCond));
    }
    if (ageCond != null) {
        builder.and(member.age.eq(ageCond));
    }

    return queryFactory
            .selectFrom(member)
            .where(builder) // 여러 빌더도 and/or 조합 가능
            .fetch();
}
```

### Where 다중 파라미터
```java
private List<Member> searchMember2(String nameCond, Integer ageCond) {
    return queryFactory
            .selectFrom(member)
            .where(nameEq(nameCond), ageEq(ageCond)) // 조건의 null값은 무시됨
            .fetch();
}

private Predicate nameEq(String nameCond) {
    if (nameCond == null) {
        return null;
    }
    return member.name.eq(nameCond);
}

private Predicate ageEq(Integer ageCond) {
    if (ageCond == null) {
        return null;
    }
    return member.age.eq(ageCond);
}
```
조건을 별도의 함수로 작성   
-> 조건 조립 가능(BooleanExpression 타입)   
-> isServiceable과 같이 유의미한 조건을 만들어서 재활용 가능

```java
private List<Member> searchMember2(String nameCond, Integer ageCond) {
    return queryFactory
            .selectFrom(member)
//            .where(nameEq(nameCond), ageEq(ageCond)) // 조건에 null이 들어가면 무시됨
            .where(allEq(nameCond, ageCond))
            .fetch();
}

private BooleanExpression nameEq(String nameCond) {
    if (nameCond == null) {
        return null;
    }
    return member.name.eq(nameCond);
}

private BooleanExpression ageEq(Integer ageCond) {
    if (ageCond == null) {
        return null;
    }
    return member.age.eq(ageCond);
}

private BooleanExpression allEq(String nameCond, Integer ageCond) {
    return nameEq(nameCond).and(ageEq(ageCond));
}
```
BooleanExpression: Predicate 상속

만약 모든 조건이 null일 경우 전체 데이터를 가져오기 때문에 페이징과 함께 만드는 것이 좋음

## 중급 문법
### 수정, 삭제 벌크 연산
쿼리 한번으로 대량의 데이터 수정   
변경 감지는 여러 쿼리를 사용해야 함

** 벌크 연산은 영속성 컨텍스트를 무시하고 DB에 반영   
-> 같은 트랜잭션 내에서 조회 시 결과가 다름
=> 벌크 연산을 하고 나면 영속성 컨텍스트를 초기화 해줌

```java
@Test
void bulkUpdate() {
    long count = queryFactory
            .update(member)
            .set(member.name, "청소년")
            .where(member.age.lt(15))
            .execute();

    assertThat(count).isEqualTo(1);

    em.flush();
    em.clear();
}
```
벌크 연산이 적용된 데이터 수 반환

### SQL function 호출
org.hibernate.dialect에 등록되어 있는 함수 사용 가능
```java
@Test
void sqlFunction() {
    List<String> result = queryFactory
            .select(
                    Expressions.stringTemplate("function('replace', {0}, {1}, {2})",
                            member.name, "member", "M")
            )
            .from(member)
            .fetch();
}
```

## 스프링 데이터 JPA 활용
### 페이징
데이터와 카운트 조회 한번에 수행
```java
public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
    QueryResults<MemberTeamDto> results = queryFactory
            .select(new QMemberTeamDto(
                    member.id,
                    member.name,
                    member.age,
                    team.id,
                    team.name
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();

    List<MemberTeamDto> content = results.getResults();
    long total = results.getTotal();

    return new PageImpl<>(content, pageable, total);
}
```

데이터와 카운트를 별도로 조회   
-> count 쿼리 최적화 가능(데이터가 많은 경우)
- join하지 않아도 되는 경우 join x
- count 쿼리 생략 가능한 경우 생략 -> PageableExecutionUtils.getPage 사용
  - 첫번째 페이지인데 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
  - 마지막 페이지일 경우(offset, 컨텐츠 사이즈로 전체 사이즈 구함)
```java
public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
    List<MemberTeamDto> results = queryFactory
            .select(new QMemberTeamDto(
                    member.id,
                    member.name,
                    member.age,
                    team.id,
                    team.name
            ))
            .from(member)
            .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    // 카운트 쿼리 최적화
    JPAQuery<Member> countQuery = queryFactory
            .selectFrom(member)
//                .leftJoin(member.team, team)
            .where(
                    usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe())
            );
//                .fetchCount();

    // count 쿼리를 생략할 수 있는 경우 fetchCount() 실행 x
//        return new PageImpl<>(results, pageable, total);
    return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchCount);
}
```

### 정렬
스프링 데이터 Sort를 Querydsl의 OrderSpecifier로 변환
```java
JPAQuery<Member> query = queryFactory.selectFrom(member);

for (Sort.Order o : pageable.getSort()) {
    PathBuilder pathBuilder = new PathBuilder(member.getType(), member.getMetadata());
    query.orderBy(new OrderSpecifier(
            o.isAscending() ? Order.ASC : Order.DESC,
            pathBuilder.get(o.getProperty())));
    }

List<Member> result = query.fetch();
```

### QuerydslPredicateExecutor
스프링 데이터 JPA 메서드 파라미터로 Predicate 조건을 넣을 수 있음

한계점
- left join 불가
- 서비스 계층이 Querydsl에 의존
- 실무에서 사용하기 어려움

### Querydsl Web
Query string으로 Predicate 조건을 받을 수 있음

한계점
- 단순한 조건만 가능
- 조건을 커스텀하기 어려움
- 컨트롤러 계층이 Querydsl에 의존
- 실무에서 사용하기 어려움

### QuerydslRepositorySupport
Querydsl을 사용하는 레포지토리에서 상속받아 사용하는 추상 클래스   
여러 유틸리티 제공   
페이징을 편리하게 사용 가능

한계점
- Querydsl 3.x 버전을 대상으로 만들어져 JPAQueryFactory를 제공하지 않음
- 정렬 기능이 정상 동작하지 않음

### Querydsl 지원 클래스 직접 생성
QuerydslRepositorySupport가 지닌 한계를 개선

- 스프링 데이터 JPA의 페이징을 편리하게 변환
- 페이징과 카운트 쿼리 분리
- 스프링 데이터 JPA 정렬 지원
- select()로 시작 가능
- EntityManager, QueryFactory 제공

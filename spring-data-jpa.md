# Spring Data JPA
## 공통 인터페이스   
JpaRepository<엔티티 타입, 식별자 타입> 상속   
스프링 데이터 JPA가 구현 클래스를 대신 생성   
@Repository 생략 가능(컴포넌트 스캔을 스프링 데이터 JPA가 자동으로 처리)

### 패키지 구조
![스크린샷 2021-10-05 오후 4 35 46](https://user-images.githubusercontent.com/68456385/135980074-ad36dbb4-49e3-4bf4-afcb-e42db00f0154.png)
spring-data-commons   
org.springframework.data.repository   
-> 공통 기능

spring-data-jpa   
org.springframework.data.jpa.repository   
-> DB 종류에 따라 다름

### 스프링 데이터 JPA 구현체
SimpleJpaRepository
- @Repository: 컴포넌트 스캔 대상, JPA 예외를 스프링이 추상화한 예외로 변환
- @Transactional: 스프링 데이터 JPA 메서드를 사용하면 트랜잭션이 없어도 데이터 등록, 변경이 가능
  - readOnly = true: 데이터를 조회만 하는 트랜잭션에서 사용하면 플러시를 생략하여 성능 최적화
```java
@Transactional
@Override
public <S extends T> S save(S entity) {

    Assert.notNull(entity, "Entity must not be null.");

    if (entityInformation.isNew(entity)) {
        em.persist(entity);
        return entity;
    } else {
        return em.merge(entity);
    }
}
```
- 새로운 엔티티면(식별자 값이 존재하지 않으면) 저장
  - persist()가 호출될 때 @GeneratedValue에 의해 식별자 값 저장
- DB에 존재하는 엔티티면(식별자 값이 존재하면) 병합
  1. 식별자로 엔티티를 조회
  2. 모든 값을 덮어씀
  3. 변경 감지에 의해 DB에 반영

**@GeneratedValue를 사용할 수 없는 경우**   
save() 호출 시 식별자 값을 넣어줘야 하므로 merge()가 호출됨   
merge()는 식별자 값으로 조회 쿼리를 수행하므로 비효율적   
-> Persistable 인터페이스를 상속받아서 isNew() 메서드 구현   
-> @CreatedDate 값 유무에 따라 판단 가능

## 쿼리 메소드 기능
### 메소드 이름으로 쿼리 생성
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByName(String name);
}
```
관례를 통해 메소드 이름을 분석하고 JPQL을 생성   
필드 명이 변경되면 인터페이스에 정의한 메서드 이름도 변경해야 함

- 조회: find...By ,read...By ,query...By get...By,
- COUNT: count...By, 반환타입 long
- EXISTS: exists...By, 반환타입 boolean
- 삭제: delete...By, remove...By, 반환타입 long 
- DISTINCT: findDistinct, findMemberDistinctBy 
- LIMIT: findFirst3, findFirst, findTop, findTop3

...에 식별하기 위한 내용이 들어가도 됨

스프링 데이터 JPA 공식 문서 참고: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation

### NamedQuery
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query(name = "Member.findByName")
    List<Member> findByName(@Param("name") String name);
}
```
구현되어 있는 NamedQuery를 찾아서 가져옴

### @Query
레포지토리 메서드에 쿼리 직접 정의    
메서드 이름을 임의로 지정 가능
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m from Member m where m.name = :name and m.age = :age")
    List<Member> findUser(@Param("name") String name, @Param("age") int age);
}
```
애플리케이션 로딩 시점에 오류를 찾을 수 있음 

**값, DTO 조회**
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m.name from Member m")
    List<String> findNameList();

    @Query("select new jpabook.jpashop.dto.MemberDto(m.id, m.name) from Member m")
    List<MemberDto> findMemberDto();
}
```

### 반환 타입
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByName(String name); // 컬렉션

    Member findMemberByName(String name); // 단건

    Optional<Member> findOptionalByName(String name); // 단건 Optional
}
```
JPA는 단건 조회 시 결과가 없으면 예외 발생   
스프링 데이터 JPA는 null을 반환

https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-return-types

### 페이징과 정렬
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    Page<Member> findPageByAge(int age, Pageable pageable);

    Slice<Member> findSliceByAge(int age, Pageable pageable);

    List<Member> findMemberByAge(int age, Pageable pageable);
}
```
- Page: 추가 count 쿼리를 날림(Slice 인터페이스를 상속하여 전체 데이터 수, 전체 페이지 수 기능 추가)
- Slice: 추가 count 쿼리 없이 다음 페이지까지 확인 가능(내부적으로 limit+1까지 조회) -> 다음 페이지 여부 확인 가능(Ex. 더보기 버튼)
- List: 추가 count 쿼리 없이 결과만 반환

Pageable: 인터페이스. 실제 사용 시에는 구현체인 PageRequest 객체 사용   
PageRequest(페이지 번호, 페이지 크기, 정렬 정보)   
페이지 번호는 0부터 시작

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    @Query(
            value = "select m from Member m left join m.team",
            countQuery = "select count(m) from Member m"
    )
    Page<Member> findPageByAge(int age, Pageable pageable);
}
```
쿼리를 직접 정의하면 count 쿼리도 정의된 쿼리를 기반으로 나감   
-> left join을 해도 count 개수는 동일하므로, left join 사용 시 count 쿼리를 별도로 분리하여 성능 최적화

**페이지를 유지하면서 엔티티를 DTO로 변환**
```java
Page<Member> page = memberRepository.findPageByAge(10, pageRequest);
Page<MemberDto> dtoPage = page.map(member -> new MemberDto(member.getId(), member.getName()));
```

**스프링 MVC**
```java
@GetMapping("/members/page")
public Page<MemberDto> pageList(Pageable pageable) {
    Page<Member> page = memberRepository.findAll(pageable);
    return page.map(MemberDto::new);
}
```
파라미터로 Pageable을 받으면 스프링에서 PageRequest 객체를 생성해서 넣어줌

요청 파라미터
- /members?page=0&size=3&sort=id&sort=name,desc
- page: 현재 페이지(0부터 시작)
- size: 한 페이지에 노출할 데이터 수
- sort: 정렬 조건

기본값 설정
- 글로벌 설정(application.yml)
```yaml
spring.data.web.pageable.default-page-size=20
```
- 개별 설정
```java
@GetMapping("/members/page")
public Page<MemberDto> pageList(@PageableDefault(size = 20) Pageable pageable) {
    ...
}
```

페이징 정보가 둘 이상이면 접두사로 구분
```java
@GetMapping("/members/page")
public Page<MemberDto> pageList(
    @Qualifier("member") Pageable memberPageable,
    @Qualifier("order") Pageable orderPageable
) {
    ...
}
```
Ex) /members?member_page=0&order_page=1



### 벌크성 수정 쿼리
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);
}
```
벌크 연산은 영속성 컨텍스트를 무시하고 DB에 반영하기 때문에 clear를 해줘야 함   
@Modifying(clearAutomatically = true) 옵션 사용 가능

### @EntityGraph
fetch join
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();
}
```
-> left join 사용

\* JPQL의 join fetch -> inner join 사용

### JPA Hint
JPA 구현체에게 제공하는 쿼리 힌트
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByName(String name);
}
```
-> 변경 감지 기능 작동 x

### JPA Lock
JPA에서 제공하는 lock 기능
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByName(String name);
}
```

## 확장 기능
### 사용자 정의 레포지토리
인터페이스의 메서드를 직접 구현하고 싶은 경우
- EntityManager 직접 사용
- JdbcTemplate
- Querydsl
```java
public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();
}
```
```java
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
}
```
```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    
}
```
** 구현 클래스의 이름은 JpaRepository를 상속한 인터페이스의 이름 + Impl 이어야 함

기능에 따라 스프링 데이터 JPA의 사용자 정의 레포지토리를 사용하지 않고 별도의 레포지트로릴 사용해도 됨

### Auditing
모든 엔티티에 공통으로 들어가는 필드
- 등록일
- 수정일
- 등록자
- 수정자

순수 JPA - @MappedSuperclass + 이벤트 애노테이션 사용

스프링 데이터 JPA      
- 설정
  - @EnableJpaAuditing -> @SpringBootApplication 클래스에 적용
  - @EntityListeners(AuditingEntityListener.class) -> 엔티티에 적용
- 이벤트 애노테이션
  - @CreatedDate
  - @LastModifiedDate
  - @CreatedBy
  - @LastModifiedBy

등록자, 수정자
```java
@EnableJpaAuditing
@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	public AuditorAware<String> auditorProvider() {
        // 임의로 랜덤 번호 부여
		return () -> Optional.of(UUID.randomUUID().toString());
	}

}
```

## 나머지 기능
### Query By Example
```java
// Probe 생성
Member member = new Member("member1");

// ExampleMatcher 생성
ExampleMatcher matcher = ExampleMatcher.matching()
        .withIgnorePaths("age");
        
// Example 생성
Example<Member> example = Example.of(member);

List<Member> result = memberRepository.findAll(example);
```
- Example: Probe와 ExampleMatcher로 구성, 쿼리를 생성하는데 사용
- Probe: 필드에 데이터가 있는 실제 도메인 객체
- ExampleMathcer: 특정 필드를 일치시키는 상세한 정보 제공

장점
- 동적 쿼리를 편리하게 처리
- 도메인 객체를 그대로 사용
- DB 종류에 의존적이지 않음

단점
- 외부 조인 불가능
- 중첩 제약조건 사용 불가
- 문자열을 제외한 다른 타입은 exact 매칭만 지원

### Projections
전체 필드가 아니라 특정 필드만 조회하고 싶은 경우
```java
public interface NameOnly {
    String getName();
}
```
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<NameOnly> findProjectionsByName(String name);
}
```
프로젝션 대상이 root 엔티티면 쿼리 최적화 가능   
연관된 엔티티까지 조회하면 left join으로 처리하기 때문에 최적화가 되지 않음

### 네이티브 쿼리
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query(value = "select * from member where name = ?", nativeQuery = true)
    List<Member> findByNativeQuery(String name);
}
```
- Sort 파라미터를 통한 정렬이 정상 동작하지 않을 수 있음
- 애플리케이션 로딩 시점에 문법 확인 불가
- 동적 쿼리 불가
- DTO로 반환하기 어려움 -> JdbcTemplate 이나 myBatis 사용

**Projections과 조합**   
```java
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query(
            value = "select name from member",
            countQuery = "select count(*) from member",
            nativeQuery = true
    )
    Page<NameOnly> findByNativeProjection(Pageable pageable);
}
```
-> 다른 반환 타입 사용 가능

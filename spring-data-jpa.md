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
레포지토리 메소드에 쿼리 직접 정의
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

페이지를 유지하면서 엔티티를 DTO로 변환
```java
Page<Member> page = memberRepository.findPageByAge(10, pageRequest);
Page<MemberDto> dtoPage = page.map(member -> new MemberDto(member.getId(), member.getName()));
```

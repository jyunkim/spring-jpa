# JPA
출처    
https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-%ED%99%9C%EC%9A%A9-1/dashboard    
https://www.inflearn.com/course/ORM-JPA-Basic/dashboard

## JPA란?
### SQL 중심적인 개발
객체지향 설계를 할수록 매핑 작업이 늘어남   

Jdbc, JdbcTemplate, MyBatis   
-> 개발자가 SQL을 직접 작성해야함

객체를 자바 컬렉션에 저장하듯이 DB에 저장할 수 없을까?

### JPA
Java Persistence API   
자바 진영의 ORM 표준   
자바 애플리케이션과 Jdbc 사이에서 동작   
인터페이스의 모음으로 대표적인 구현체로 Hibernate가 있음

**ORM**   
Object-relational mapping(객체 관계 매핑)  
ORM 프레임워크가 중간에서 객체와 테이블을 매핑

### JPA의 장점
1. SQL 중심적인 개발에서 객체 중심으로 개발   
    - 특정 DB에 종속 x -> DB에 상관없이 일관된 함수 사용(hibernate.dialect 속성에 지정)
    - 변경 감지: 자바 컬렉션을 다루는 것처럼 데이터 수정 후 따로 persist를 하지 않아도 됨. 
    트랜잭션이 커밋되는 시점에 엔티티와 스냅샷을 비교하여 변경사항이 있으면 update SQL을 생성하여 반영 
   
   
2. 생산성   
매우 간단한 코드


3. 유지보수   
필드 변경 시 SQL 수정 필요 x


4. 패러다임의 불일치 해결
    - 상속: 자동으로 join 등 여러 테이블에 쿼리를 날림
    - 연관관계: 자유로운 객체 그래프 탐색, 신뢰할 수 있는 엔티티
    - 비교: 동일한 트랜잭션에서 조회한 엔티티는 같음을 보장
  

5. 성능 최적화
    - 1차 캐시와 동일성 보장: 동일한 트랜잭션에서 다시 조회 시 캐싱을 통해 DB에 접근하지 않아 SQL이 한번만 실행됨
    - 트랜잭션을 지원하는 쓰기 지연: 트랜잭션을 커밋할 때까지 쿼리를 쌓아놨다 한번에 SQL 적용 -> 네트워크를 여러번 타지 않아도 됨
    - 지연 로딩: 객체가 실제로 사용될 때 로딩(조회 쿼리를 분리해서 보냄)   
\* 즉시 로딩: join으로 연관된 객체까지 한번에 조회   
      ![image](https://user-images.githubusercontent.com/68456385/132205556-4c5a9fe0-786c-4f81-bd81-0b171da8b526.png)

### JPA 구동 방식
![image](https://user-images.githubusercontent.com/68456385/132178586-d4efbc1c-393f-4dd9-a8b4-0c299b774fa7.png)

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello"); // persistence-unit name

EntityManager em = emf.createEntityManager();

EntityTransaction tx = em.getTransaction();
tx.begin();

try {
    Member member = new Member();
    member.setName("memberA");
    em.persist(member);
    tx.commit();
} catch (Exception e) {
    tx.rollback();
} finally {
    em.close();
}
emf.close();
```
Entity Manager Factory는 애플리케이션 전체에서 하나를 공유   
Entity Manager는 요청이 올 때마다 생성(스레드 간 공유 x)

**JPA의 모든 데이터 변경은 트랜잭션안에서 실행되어야 함**

**JPQL**   
SQL을 추상화한 객체 지향 쿼리 언어   
테이블이 아닌 엔티티를 대상으로 쿼리

## 영속성 관리
### 영속성 컨텍스트
엔티티를 영구 저장하는 환경   
엔키키 매니저를 통해 접근   
컨테이너 환경에서는 여러 엔티티 매니저가 하나의 영속성 컨텍스트를 관리    
매 트랜잭션마다 생성되고, 트랜잭션이 끝나면 종료됨
```java
em.persist(entity);
```
-> 엔티티를 영속성 컨텍스트에 영속   
1차 캐시에 먼저 저장하고, 커밋 이후에 DB에 저장됨

### 엔티티의 생명주기
- 비영속: 영속성 컨텍스트와 관계없는 새로운 상태
- 영속: 영속성 컨텍스트에 관리되는 상태(저장, 조회 시)
- 준영속: 영속성 컨텍스트에 저장되었다가 분리된 상태
  - em.detach(entity): 특정 엔티티를 준영속 상태로 전환
  - em.clear(): 영속성 컨텍스트를 초기화
  - em.close(): 영속성 컨텍스트를 종료
- 삭제: 삭제된 상태

### 영속성 컨텍스트의 역할
1. 1차 캐시

2. 동일성 보장
   
3. 트랜잭션을 지원하는 쓰기 지연(Transactional write-behind)

4. 변경 감지(Dirty Checking)

5. 지연 로딩(Lazy Loading)

### 플러시
영속성 컨텍스트의 변경내용을 DB에 반영   
트랜잭션 커밋, JPQL 쿼리 실행 시 호출   
또는 em.flush()로 호출 가능

- 변경 감지
- 쓰기 지연 SQL 저장소의 쿼리를 DB에 전송

영속성 컨텍스트를 비우는 것은 아님 - em.clear()

## 엔티티 매핑
### 객체와 테이블 매핑
**@Entity**   
JPA가 관리하는 엔티티   
테이블과 매핑할 클래스   
기본 생성자 필요(public 또는 protected) -> 리플렉션   
final, enum, interface, inner 클래스 사용 x

**JPA 엔티티는 final class이면 안된다.**   
-> 지연 로딩 방식을 이용하여 데이터를 조회하기 위해 엔티티를 상속한 프록시 객체를 생성

**JPA 엔티티는 public, protected 기본 생성자를 포함해야 하고, 필드 역시 final이면 안된다.**   
-> JPA는 프록시 객체를 생성할 때 Reflection API를 이용, java Reflection이 가져올 수 없는 정보 중 하나가 바로 생성자의 인자 정보이므로, 
reflection으로 객체를 생성하기 위해서는 기본 생성자가 필요. 또한 생성된 프록시 객체의 필드를 초기화하기 위해서는 필드가 final이면 안됨

** final 필드는 반드시 선언과 함께 초기화되거나 생성자를 이용하여 초기화 되어야 함

**Java Reflection**   
구체적인 클래스 타입을 알지 못해도, 그 클래스의 메소드, 타입, 변수들에 접근할 수 있도록 해주는 자바 API로, 
이미 로딩이 완료된 클래스에서 또 다른 클래스를 동적으로 로딩하여 생성자, 멤버 필드, 멤버 메서드등을 사용할 수 있도록 함      
Ex) Object s = new String() -> s라는 객체는 String 클래스에 대해 전혀 알지 못하고, Object 타입이라는 사실만 앎
(자바는 정적언어로 컴파일 시점에 타입이 결정되기 때문)

### 데이터베이스 스키마 자동 생성
DDL을 애플리케이션 실행 시점에 자동 생성   
데이터베이스 방언을 활용하여 DB에 맞는 적절한 DDL 생성

spring.jpa.hibernate.ddl-auto
![image](https://user-images.githubusercontent.com/68456385/132302678-f8de76be-cfc5-4a42-bb64-1a7811b1742e.png)

**운영 단계에서는 절대 create, create-drop, update를 사용하면 안됨**   
- 개발 초기 단계 - create, update
- 테스트 단계 - update, validate
- 운영 단계 - validate, none

**@Table**   
엔티티와 매핑할 테이블 설정
![image](https://user-images.githubusercontent.com/68456385/132307415-550bbda9-3fd7-4dfd-9381-d203c80de2b4.png)

### 필드와 컬럼 매핑
**@Column**   
칼럼 제약조건 추가
![image](https://user-images.githubusercontent.com/68456385/132305507-03a090c0-8617-40e8-b1bc-410724511ef7.png)   
unique 옵션은 제약조건 이름이 임의의 문자열로 나오기 때문에 @Table 제약조건을 사용   
@Table(uniqueConstraints = {@UniqueConstraint( name = "NAME_AGE_UNIQUE",
columnNames = {"NAME", "AGE"} )})

**@Enumerated**   
Enum 타입 매핑   
EnumType
- ORDINAL: enum 순서를 저장
- STRING: enum 이름을 저장

enum 값이 추가될 경우 순서가 꼬이기 때문에 STRING 사용

**@Temporal**   
Date 타입 매핑   
TemporalType
- DATE: 날짜
- TIME: 시간
- TIMESTAMP: 날짜, 시간

최신 hibernate에서 지원하는 LocalDate, LocalDateTime 사용 시 생략 가능

**@Lob**   
Text형(CLOB, BLOB)과 매핑

**@Transient**   
필드 매핑 x -> 칼럼 생성 x

### 기본 키 매핑
기본 키 제약 조건: not null, unique, not change   
-> 이 조건을 만족하는 자연키(주민번호)는 찾기 어려움   
-> 임의의 대체키 사용   
-> Long형 대체키 + 키 생성 전략 사용

**@Id**   
Primary key와 매핑

**@GeneratedValue**   
자동 생성   
strategy 설정 가능   
GenerationType
- AUTO(기본값): DB 방언에 맞춰서 생성
- IDENTITY: 기본 키 생성을 DB에 위임(Ex. AUTO_INCREMENT). 
  영속성 컨텍스트에 저장되기 위해서는 기본 키값이 있어야 하므로 persist 시점에 즉시 insert SQL을 실행하고 DB에서 식별자를 조회   
  -> 쓰기 지연 전략 사용 불가   
  주로 MySQL에서 사용(대부분 사용)
- SEQUENCE: DB 시퀀스 오브젝트 사용. 시퀀스 오브젝트에서 값을 가져와 기본 키에 저장. @SequenceGenerator 필요.
  테이블을 파티션할 때는 IDENTITY를 사용할 수 없기 때문에 SEQUENCE를 사용해야 함   
  데이터 규모가 커지면 redis를 이용하여 키 발급 시스템을 만들어서 사용
  ![image](https://user-images.githubusercontent.com/68456385/132311918-a6b4cee2-f97c-45b4-a0fb-56000117ace6.png)   
  allocationSize: DB에서 50씩 증가시키고 애플리케이션에서 1씩 증가시켜 사용 -> 메모리에만 접근
- TABLE: 키 생성 테이블을 하나 생성해서 사용. @TableGenerator      
-> 모든 DB에 적용 가능하지만, 성능 문제 발생

## 연관관계 매핑
테이블의 외래 키를 객체에 그대로 가져오는 것보다는 참조값을 가져오는 것이 좋음(find를 다시 하지 않아도 됨)   
-> 객체의 참조와 테이블의 외래 키를 매핑

### 단방향 연관관계
![image](https://user-images.githubusercontent.com/68456385/132320425-378b025d-40f4-4b7d-a99e-70ada7d49e77.png)

```java
@ManyToOne
@JoinColumn(name = "TEAM_ID")
private Team team;
```

### 양방향 연관관계
![image](https://user-images.githubusercontent.com/68456385/132321420-6215bb33-6aac-448d-80ad-27801fb41602.png)

객체를 양방향으로 참조하려면 단방향 연관관계 2개를 만들어야함   
테이블은 외래 키 하나로 양방향 연관관계를 가짐

객체 연관관계
- 회원 -> 팀(단방향)
- 팀 -> 회원(단방향)

테이블 연관관계
- 회원 <-> 팀(양방향)

두 참조값 중 어떤 것을 외래 키와 매핑할 것인가?

**연관관계 주인**   
두 참조값 중 하나를 연관관계 주인으로 지정   
연관관계의 주인만이 외래 키를 관리 -> 등록, 수정 가능  
주인이 아닌 쪽은 읽기만 가능 -> 값을 넣어도 외래 키에 반영 x   
mappedBy 속성으로 주인 지정

누구를 주인으로?   
-> 다른 엔티티의 필드값을 수정했는데 컬럼값이 바뀌면 헷갈림   
-> 외래 키가 있는 엔티티의 필드를 주인으로 설정

![image](https://user-images.githubusercontent.com/68456385/132323729-0ea72310-eb44-4279-b8d0-cad94be66bd1.png)

```java
@OneToMany(mappedBy = "team")
List<Member> members = new ArrayList<Member>();
```

**주의점**   
List에 값을 넣지 않고 연관관계 주인에만 값을 넣어도 커밋이 되면 
지연로딩을 통해 list 조회 시 연관된 객체를 모두 가져와 list에 삽입  

But, 같은 트랜잭션 안에서는 조회가 되지 않기 때문에 양쪽에 값을 넣는 것이 좋음   
-> 한 쪽에 연관관계 편의 메서드 생성
```java
public void addMember(Member member) {
    members.add(member);
    member.setTeam(this);
}
```

양방향 매핑 시 무한 루프 주의
- toString(): 양쪽의 toString()을 계속 호출 -> 구현 x
- JSON 생성 라이브러리: 필드가 json으로 계속 생성 -> Controller에서 엔티티를 직접 반환 x

웬만하면 단방향 연관관계로 설계하고 비즈니스적으로 필요한 곳에만 양방향 연관관계로 설정
(Ex. Order - OrderItem)

### N:1
@ManyToOne   
외래 키는 N쪽에 둠   
양방향 연관관계의 경우 N쪽을 연관관계 주인으로 설정

### 1:N
@OneToMany   
거의 사용하지 않음   
테이블에서 외래 키는 N쪽에 있지만 객체에서의 참조는 1쪽에 있음   
-> 참조값을 삽입하면 다른 테이블로 update 쿼리 추가 실행   
일대다 단방향 매핑보다는 다대일 양방향 매핑 사용

### 1:1
@OneToOne   
외래 키가 어느쪽에 있어도 됨   
-> 주가 되는 쪽(더 많이 조회되는 쪽)에 외래 키를 두는 것이 좋음

### N:M
@ManyToMany   
@JoinTable로 연결 테이블 지정   
RDB에서 다대다 관계 표현 불가 -> 연결 테이블 생성   
연결 테이블에 추가 정보를 넣을 수 없기 때문에 사용하지 않는 것이 좋음   
-> 연결 테이블 엔티티를 추가하고 일대다, 다대일로 매핑. 기본 키도 따로 생성하는 것이 좋음

## 고급 매핑
### 상속관계 매핑
객체의 상속 구조와 DB의 슈퍼, 서브 타입 관계를 매핑

**DB 구현 방법**   
@Inheritance(strategy=InheritanceType.xxx)
1. 조인 전략(JOINED)   
각각 테이블로 변환   
장점   
   - 테이블 정규화
   - 참조 무결성 제약조건 활용 가능
   - 저장 공간 효율화

    단점
   - 조회 시 조인을 많이 사용
   - 조회 쿼리가 복잡
   - 데이터 저장 시 insert 쿼리 2번 호출


2. 단일 테이블 전략(SINGLE_TABLE)   
통합 테이블로 변환   
장점
    - 조인이 필요 없어 조회 성능이 좋음
    - 조회 쿼리가 단순
    - NoSQL로 전환하기 쉬움

    단점
    - null을 허용해야 함
    - 테이블이 복잡해짐


3. 구현 클래스마다 테이블 전략(TABLE_PER_CLASS)   
서브타입 테이블로 변환   
상위 클래스 타입으로 조회 시 union으로 모든 테이블을 가져와 찾음     
자식 테이블을 통합하여 쿼리를 보내기 어려움

구현 클래스마다 테이블 전략은 지양

### 매핑 정보 상속
공통 매핑 정보가 필요할 때 사용(여러 엔티티에 중복된 필드)   
Ex) 등록일, 수정일

**@MappedSuperclass**      
엔티티가 아니므로 테이블과 매핑되지 않음   
자식 클래스에 매핑 정보만 제공   
조회, 검색 불가   
추상 클래스로 사용

## 프록시와 연관관계 관리
### 프록시
가짜 객체   
실제 클래스를 상속 받아서 만들어짐 -> 타입 체크시 ==이 아닌 instanceof 사용      
프록시 객체는 실제 객체의 참조(target)를 보관   
프록시 객체의 메서드를 호출하면 프록시 객체는 실제 객체의 메서드 호출
- em.find(): DB를 통해 실제 엔티티 객체 조회
- em.getReference(): 프록시 엔티티 객체 조회. DB 조회는 지연시킴

1. em.getReference(Member.class, 1) 호출
2. 영속성 컨텍스트에 Member 타입의 target 필드와 Member의 메서드를 가지고 있는 MemberProxy 객체가 생성됨. 
getId()를 제외한 메서드는 모두 비어있고, target도 null을 가리킴
3. getId() 외에 다른 메서드를 호출하면 실제 객체의 메서드를 호출해야 하지만 target이 비어있기 때문에 영속성 컨텍스트에 초기화 요청
4. 쿼리를 날려서 DB를 조회하고 실제 객체를 생성해줌
5. target 필드에 실제 객체의 참조값이 들어감
6. 이 이후에는 프록시 객체의 메서드를 호출하면 실제 객체의 메서드를 호출(초기화는 한번만 수행)

** 영속성 컨텍스트(1차 캐시)에 엔티티가 존재하면 em.getReference()를 호출해도 프록시 객체를 반환하지 않고 실제 엔티티 반환. 
반대로 영속성 컨텍스트에 프록시가 존재하면 em.find()를 호출해도 프록시 객체를 반환

** 트랜잭션 밖이라 영속성 컨텍스트가 없거나, 프록시 객체가 준영속 상태일 때 프록시를 초기화할 수 없음

** 프록시 객체는 필드를 사용할 수 없으므로 equals() 등의 메서드를 구현할 때 getter를 사용해서 구현하는 것이 좋음

### 프록시 유틸리티 함수
프록시 인스턴스 초기화 여부   
emf.getPersistenceUnitUtil.isLoaded(entity)

프록시 클래스 확인   
entity.getClass()

프록시 강제 초기화   
Hibernate.initialize(entity)

### 지연 로딩
```java
@Entity
class Member {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}
```
엔티티(Member) 조회 시(em.find()) 연관된 엔티티(Team)를 프록시로 가져옴    
-> 연관된 엔티티에 대한 조회 쿼리는 연관된 엔티티의 getter를 호출할 때 수행됨

즉시 로딩을 사용할 경우 join으로 연관된 엔티티를 하나의 쿼리로 가져옴

- @ManyToOne, @OneToOne - 기본이 즉시 로딩   
- @OneToMany, @ManyToMany - 기본이 지연 로딩

**실무에서는 가급적 지연 로딩만 사용**   
즉시 로딩을 적용하면 예상하지 못한 쿼리가 발생   
즉시 로딩은 JPQL에서 N+1 문제를 일으킴

**N+1 쿼리 문제**   
연관관계가 설정된 엔티티를 조회할 경우, 조회된 데이터 개수만큼 연관관계 조회 쿼리가 추가로 발생하는 문제   
jpaRepository에 정의한 인터페이스 메서드를 실행하면 JPA는 메서드 이름을 분석해서 JPQL을 생성하여 실행하게 된다. 
JPQL은 SQL을 추상화한 객체지향 쿼리 언어로서 특정 SQL에 종속되지 않고 엔티티 객체와 필드 이름을 가지고 쿼리를 한다. 
그렇기 때문에 JPQL은 findAll()이란 메소드를 수행하였을 때 연관관계 데이터를 무시하고 해당 엔티티를 조회하는 쿼리만 실행한다. 
연관된 엔티티 데이터가 필요한 경우, FetchType으로 지정한 시점에 조회를 별도로 호출하여 N+1 문제가 발생한다.

FetchType과는 관련이 없음   
-> FetchType을 변경하는 것은 단지 N+1 발생 시점을 연관관계 데이터를 사용하는 시점으로 미룰지, 아니면 초기 데이터 로드 시점에 가져오느냐에 차이만 있음   
-> 지연 로딩으로 설정한 뒤 fetch join 이나 엔티티 그래프 기능을 사용

### 영속성 전이
```java
@OneToMany(mappedBy="parent", cascade=CascadeType.PERSIST)
```
특정 엔티티를 영속 상태로 만들 때 연관된 엔티티도 함께 영속 상태로 만듬   
생성 외 다른 동작도 가능하며, 두 엔티티가 생명주기를 함께 하게됨

CASCADE 종류
- All: 모두 적용
- PERSIST: 영속
- REMOVE: 삭제

OrderItem과 같이 Order에서만 사용하는 종속적인 엔티티에만 부여(또 다른 엔티티와 연관관계가 있으면 안됨)

### 고아 객체
부모 엔티티와 연관관계가 끊어진 자식 엔티티

고아 객체 자동 제거
```java
@OneToMany(mappedBy="parent", orphanRemoval=true)
List<Child> childList = new ArrayList<>();
```
-> 리스트에서 자식 엔티티를 제거하면 delete 쿼리가 나가서 자식 엔티티 삭제

영속성 전이와 마찬가지로 특정 엔티티가 혼자만 소유하는 엔티티, 연관관계가 오직 하나인 엔티티에만 사용

**orphanRemoval=true vs CascadeType.REMOVE**   
orphanRemoval은 child 엔티티에 null을 주었을 때도 제거   
@OneToOne, @OneToMany만 사용 가능

## 값 타입
### JPA의 데이터 타입 분류
엔티티 타입
- @Entity로 정의하는 객체
- 데이터가 변해도 식별자로 지속해서 추적 가능

값 타입
- 자바 기본 타입이나 객체
- 식별자가 없고 값만 있으므로 변경 시 추적 불가
- 자신을 소유한 엔티티에 생명주기를 의존

### 기본값 타입
자바 기본 타입, 래퍼 클래스 타입   
생명주기를 엔티티에 의존    
값 타입은 공유되면 안됨   
- 원시 타입은 공유될 수 없음
- 래퍼 클래스나 String은 공유될 수 있지만 변경이 불가

### 임베디드 타입
새로운 값 타입을 직접 정의할 수 있음   
주로 기본 값 타입을 모아서 만들어 복합 값 타입이라고도 함

필드를 추상화한 뒤 값 타입을 만들어서 적용

![스크린샷 2021-09-30 오후 3 24 35](https://user-images.githubusercontent.com/68456385/135398749-a4bda130-2a11-4901-9f1a-4169b715b6fe.png)
![스크린샷 2021-09-30 오후 3 27 43](https://user-images.githubusercontent.com/68456385/135399102-b97699c3-03dd-41b2-993b-7c4bb0c26fea.png)

- @Embeddable: 값 타입을 정의하는 곳에 표시
- @Embedded: 값 타입을 사용하는 곳에 표시

엔티티와 마찬가지로 기본 생성자가 필요   
테이블에는 변화 x   
값 타입은 equals() 메서드를 통해 동등성 비교해야 함

**장점**   
- 재사용
- 높은 응집
- 값 타입만 사용하는 의미 있는 메서드 생성 가능

@AttributeOverrides   
한 엔티티에서 같은 값 타입 중복 사용 가능

**임베디드 타입을 여러 엔티티에서 공유하는 경우**      
값 타입의 실제 인스턴스를 공유하는 것은 위험 -> 한쪽에서 변경 시 공유하는 모든 곳에서 변경됨   
=> 값 타입을 불변 객체로 정의
- 생성자로만 값을 설정하고 setter를 만들지 않음
- 인스턴스는 공유하지 않고 복사해서 사용

### 값 타입 컬렉션
값 타입을 컬렉션에 담아서 사용   
-> DB에서는 컬렉션을 저장할 수 없기 때문에 별도의 테이블로 분리해야 함. 값 타입은 별도의 id를 가질 수 없으므로 전체 컬럼을 pk로 사용

- @ElementCollection, @CollectionTable 사용
- 값 타입 컬렉션은 기본으로 지연 로딩 전략 사용
- 값 타입 컬렉션의 값 타입은 주인 엔티티에 생명주기를 의존   
-> 영속성 전이와 고아 객체 제거 기능을 기본으로 가지고 있음

**문제점**   
- 값 타입 컬렉션에 변경사항이 생기면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 변경사항을 반영하여 모든 데이터를 다시 저장
- 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 pk를 구성해야 함   
-> null 입력 불가, 중복 저장 불가

=> 값 타입 컬렉션 대신 일대다 관계로 엔티티를 만들고, 여기에 값 타입을 사용   
+ 영속성 전이, 고아 객체 제거를 사용

## 객체지향 쿼리 언어
### JPA 쿼리 방법
- JPQL
- JPA Criteria
- QueryDSL
- 네이티브 SQL
- JDBC, MyBatis, SpringJdbcTemplate

### JPQL
JPA 사용 시 문제는 검색 쿼리   
모든 데이터를 객체로 변환해서 검색하는 것은 불가능   
-> SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어 제공   
-> 특정 DB에 종속되지 않음   
-> SQL 문법과 유사하지만 엔티티 객체를 대상으로 쿼리

### JPA Criteria
JPQL은 결국 문자열이기 때문에 동적 쿼리를 수행하기 어려움

JPA Criteria는 문자열이 아닌 자바 코드로 JPQL을 작성할 수 있어 동적 쿼리를 생성하기 쉬움   
But, 복잡하고 가독성이 떨어져 유지보수하기 어려움   
-> 거의 사용 x

### QueryDSL
문자열이 아닌 자바 코드로 JPQL 작성 가능   
컴파일 시점에 SQL 문법 오류를 찾을 수 있음   
동적 쿼리를 작성하기 쉬움   
코드 가독성이 좋음

### JdbcTemplate
위 방법으로 해결할 수 없는 경우 사용

** 단, 영속성 컨텍스트를 적절한 시점에 강제로 플러시 해줘야 함

### JPQL 기본 문법
- 엔티티와 속성은 대소분자 구분
- JPQL 키워드는 대소문자 구분 x
- 테이블 이름이 아닌 엔티티 이름 사용
- 별칭 필수(as 생략 가능)
- 엔티티를 직접 사용하면 식별자로 반환되어 SQL 생성   
select m from Member m where m = :member

createQuery 리턴 타입
- TypedQuery: 반환 타입이 명확할 때
- Query: 반환 타입이 명확하지 않을 때(타입이 다른 컬럼 조회)   
-> 인자로 클래스 타입 지정 x

리턴 타입 변환
- query.getResultList(): 결과가 하나 이상일 때   
  결과가 없으면 빈 리스트 반환
- query.getSingleResult(): 결과가 하나일 때   
  결과가 없거나 둘 이상이면 예외 발생
  
### 프로젝션
SELECT 절에 조회할 대상을 지정   
대상: 엔티티, 임베디드 타입, 스칼라 타입

- select m from Member m
- select m.team from Member m
- select m.address from Member m
- select m.name, m.age from Member m

**여러 타입의 값 조회**   
1. Query 타입으로 조회
2. Object[] 타입으로 조회
3. new 명령어로 조회 -> 권장
    - 값을 DTO에 담아서 조회
    - 패키지 명을 포함한 전체 클래스 명 입력
    - 파라미터 순서와 타입이 일치하는 생성자 필요

### 페이징
- setFirstResult(int startPosition): 조회 시작 위치(0부터 시작)
- setMaxResults(int maxResult): 조회할 데이터 수

### 조인
- 내부 조인   
select m from Member m join m.team t
- 외부 조인   
select m from Member m left join m.team t
- 세타 조인   
select m from Member m, Team t where m.name = t.name
  
**ON 절**   
- 조인 대상을 필터링할 경우   
Ex) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인   
  select m, t from Member m left join m.team t on t.name = 'A'
- 연관관계가 없는 엔티티를 외부 조인할 경우   
Ex) 회원의 이름과 팀의 이름이 같은 대상 외부 조인   
  select m, t from Member m left join Team t on m.name = t.name

### 서브 쿼리
메인 쿼리와 서브 쿼리는 alias를 따로 정의해서 쓰는게 성능이 좋음   
Ex) select m from Member m where m.age > (select avg(m2.age) from Member m2)

**서브쿼리 지원 함수**   
- (NOT) EXISTS (subquery)   
  select m from Member m where exists (select t from m.team t where t.name = 'A')
- {ALL | ANY} (subquery)   
  select o from Order o where o.orderAmount > all (select p.stockAmount from Product p)
- (NOT) IN (subquery)

**JPA 서브 쿼리 한계**   
JPA는 select, where, having 절에서만 서브 쿼리 사용 가능(from 절에서 불가능)   
-> join으로 풀어서 해결하거나 쿼리를 두번 날려서 해결하거나 native query 사용

### 타입 표현
Enum 타입 사용 시 패키지 명까지 포함해서 작성해야 함

### 조건식
기본 case 식
```
select
    case when m.age < 10 then '학생요금'
         when m.age > 60 then '경로요금'
         else '일반요금'
    end
from Member m
```

단순 case 식
```
select
    case t.name
        when 'A' then '인센티브110%'
        when 'B' then '인센티브120%'
        else '인센티브100%'
    end
from Team t
```

COALESCE: 하나씩 조회해서 null이 아니면 반환   
select coalesce(m.name, '이름 없는 회원') from Member m

NULLIF: 두 인자가 같으면 null 반환, 다르면 첫 번째 인자 반환   
select nullif(m.name, '관리자') from Member m

### 함수
**JPQL 기본 함수**   
concat, substring, trim, length, locate, lower, upper 등

**사용자 정의 함수**   
하이버네이트는 사용 전 방언에 추가해야 함   
사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록

### 경로 표현식
.을 통해 객체 그래프를 탐색하는 것

- 상태 필드: 단순히 값을 저장하기 위한 필드(Ex. m.name)   
-> 경로 탐색의 끝, 더이상 탐색 불가
- 연관 필드
  - 단일 값 연관 필드: 대상이 엔티티(Ex. m.team)   
  -> 묵시적 내부 조인 발생, 또 탐색 가능
  - 컬렉션 값 연관 필드: 대상이 컬렉션(Ex. t.members)   
  -> 묵시적 내부 조인 발생, 더이상 탐색 불가능   
  -> FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 사용 가능   
  Ex) select m.username from Team t join t.members m

**묵시적 조인**   
경로 표현식에 의해 묵시적으로 SQL 조인이 발생하는 것(내부 조인만 가능)   
묵시적 조인이 사용되면 쿼리 튜닝이 어려워지기 때문에 지양해야 함   
조인은 SQL 튜닝에 매우 중요한 포인트   
-> join 키워드를 직접 사용하는 명시적 조인을 사용

### fetch join
- SQL 조인 종류가 아님
- JPQL에서 성능 최적화를 위해 제공
- 연관된 엔티티나 컬렉션을 쿼리 하나로 함께 조회하는 기능
- 객체 그래프를 SQL 한번에 조회
- join fetch 명령어로 사용   
select m from Member m join fetch m.team   
-> SELECT M.*, T.* FROM MEMBER M INNER JOIN TEAM T ON M.TEAM_ID=T.ID

사용하는 경우
- em.find()   
pk를 인자로 입력받기 때문에 연관관계가 있을 시 JPA 내부에서 최적화를 통해 join으로 한번에 가져옴   
지연 로딩 - join을 사용하지 않고 연관 객체를 프록시로 가져온 후 프록시의 메서드를 호출할 때 조회 쿼리를 추가로 날림
- JPQL   
입력받은 쿼리 문자열이 그대로 SQL로 변환되기 때문에 연관관계가 있을 시 join을 사용하지 않고 조회 쿼리를 추가로 날림   
만약 조회한 데이터가 여러개일 경우 조회된 데이터 각각 추가 쿼리가 나가서 최대 n개(조회한 데이터 수)의 추가 쿼리 발생 -> N+1 문제
(같은 트랜잭션 안에서 한번 조회된 연관관계 객체는 캐시를 통해 추가 쿼리 발생 x)      
지연 로딩 - 연관 객체를 프록시로 가져온 후 프록시 메서드를 호출할 때 추가 조회 쿼리 수행   
그럼 직접 join을 써준다면?    
-> SELECT절에 지정한 엔티티만 조회하고 연관된 엔티티를 함께 조회하지 않음   
=> N+1문제 발생   
-> DTO로 변환하여 필요한 데이터만 조회

즉시 로딩 방식은 예상하지 못한 SQL이 발생하므로 지연 로딩 사용   
만약 비즈니스에서 연관관계 객체를 함께 사용하는 경우가 많으면 fetch join을 이용해서 성능 최적화   
여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야 한다면 일반 조인을 사용하고 
필요한 데이터만 조회해서 DTO로 반환(fetch join은 엔티티의 특정 부분만 projection 할 수 없음)

**컬렉션 페치 조인**   
select t from Team t join fetch t.members      
** 주의: 한 팀에 여러 멤버가 있으면 같은 팀 객체가 중복으로 가져와짐   
-> DISTINCT 사용 => SQL뿐만 아니라 애플리케이션에서도 중복을 제거하여 같은 식별자를 가진 엔티티를 제거

**페치 조인 한계**   
페치 조인 대상에는 별칭을 사용하지 않아야 함(정합성 문제)   
둘 이상의 컬렉션은 페치 조인할 수 없음   
컬렉션 페치 조인은 페이징 API를 사용하지 않아야 함   
-> 컬렉션 페치 조인 시 페이징을 사용하면 데이터가 잘리게 되어 Entity를 만들 수 없음. 따라서 모든 데이터를 조회한 후 메모리에서 페이지 처리를 하게 됨. 데이터가 많을 경우 OOM 발생 가능

**페이징 API를 사용하기 위해 컬렉션 페치 조인을 사용하지 않고 N+1 문제를 어떻게 해결할까?**      
배치 사이즈 설정   
- @BatchSize(size = 100): 일대다 연관관계 필드에 설정. 사이즈는 1000 이하
- 글로벌 세팅 적용   
-> 조회된 팀마다 멤버 조회 쿼리를 날리는게 아니라 멤버를 조회할 때 배치 사이즈만큼의 팀을 한번에 가져와서 조

### 다형성 쿼리
TYPE   
조회 대상을 특정 자식으로 한정   
select i from Item i where type(i) in (Book, Movie)

TREAT   
select i from Item i where treat(i as Book).author='kim'

### Named 쿼리
```java
@Entity
@NamedQuery(
        name = "Member.findByUsername",
        query = "select m from Member m where m.name = :name"
)
public class Member {}
```
```java
em.createNamedQuery("Member.findByUsername", Member.class)
        .setParameter("name", "회원1")
        .getResultList();
```
- 미리 정의해서 이름을 부여해두고 사용하는 JPQL   
- 정적 쿼리만 사용 가능
- 애플리케이션 로딩 시점에 초기화 후 캐싱을 통해 재사용됨
- 애플리케이션 로딩 시점에 쿼리를 검증 -> 오류를 빨리 찾아낼 수 있음

### 벌크 연산
변경 감지 기능으로 update를 하면 변경된 데이터 개수만큼 SQL이 실행

```java
int resultCount = em.createQuery("update Member m set m.age = m.age + 1")
        .executeUpdate();
```
- 쿼리 한번으로 여러 엔티티의 데이터를 변경
- executeUpdate()는 변경된 엔티티 수 반환
- update, delete 지원
- 하이버네이트에서는 insert까지 지원

벌크 연산은 영속성 컨텍스트를 무시하고 DB에 직접 쿼리   
-> 벌크 연산을 먼저 실행하거나 벌크 연산 수행 후 영속성 컨텍스트 초기화(em.clear())   
벌크 연산도 JPQL이기 때문에 수행 전에는 자동으로 flush됨

## 프로젝트 설정
### 프로젝트 생성
- 프로젝트 선택
  - Project: Gradle Project
  - Spring Boot: 2.5.x
  - Language: Java
  - Packaging: Jar
  - Java: 11
- Project Metadata
  - groupId: jpabook
  - artifactId: jpashop
- Dependencies: Spring Web, Thymeleaf, Spring Data JPA, H2 Database, Lombok, Validation, Spring Boot DevTools

\* DevTools: 코드 수정 후 recompile만 재시작하지 않아도 반영됨

### 쿼리 파라미터 로그 남기기
1. application.yml   
   logging.level.org.hibernate.type = trace
   
2. 외부 라이브러리   
   https://github.com/gavlyukovskiy/spring-boot-data-source-decorator   
   implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.6' 추가
   
## 도메인 분석 설계
### 요구 사항 분석
**기능 목록**
- 회원 기능
    - 회원 등록
    - 회원 조회
- 상품 기능
    - 상품 등록
    - 상품 수정
    - 상품 조회
- 주문 기능
    - 상품 주문
    - 주문 내역 조회
    - 주문 취소
- 기타 요구사항
    - 상품은 재고 관리가 필요하다.
    - 상품의 종류는 도서, 음반, 영화가 있다.
    - 상품을 카테고리로 구분할 수 있다.
    - 상품 주문시 배송 정보를 입력할 수 있다.
    
### 도메인 모델
![image](https://user-images.githubusercontent.com/68456385/130248266-aaefd109-7d44-42d5-b51a-f652e4f51c76.png)
다대다 관계는 관계형 데이터베이스는 물론이고 엔티티에서도 거의 사용하지 않음   
-> 주문상품이라는 엔티티를 추가해서 다대다 관계를 일대다, 다대일 관계로 풀어냄

### 엔티티 분석
![image](https://user-images.githubusercontent.com/68456385/130248854-ec55b6c5-11b5-4de5-bea5-3e0bbc3fdb88.png)
Address - 임베디드 타입(값 타입)

**개선 사항**(여기선 다양한 예제를 보여주기 위해 그냥 사용)   
- 가급적 양방향 연관관계를 사용하지 말고 단방향 연관관계 사용   
  -> 회원이 주문을 참조하지 않고, 주문만 회원을 참조
- 다대다 관계는 일대다, 다대일 관계로 풀어내서 사용(필드를 추가할 수 없기 때문)

### 테이블 분석
![image](https://user-images.githubusercontent.com/68456385/130248937-3cf6113f-2ab0-41fd-b7ec-dd9dd8bbd9c7.png)

> 테이블명이 ORDER가 아니라 ORDERS인 것은 데이터베이스가 ORDER BY를 예약어로 잡고 있는 경우가 많기 때문이다.

양방향 연관관계 엔티티에는 참조값이 2개가 있지만 테이블에는 외래키가 하나만 존재   
-> 엔티티의 참조값 중 어떤 값이 변경되었을 때 테이블의 외래키값을 변경할지 알려주어야 함   
-> 연관관계 주인 지정

외래 키가 있는 곳을 연관관계의 주인으로 정하자

일대일 관계에서는 주가 되는(접근이 많은) 테이블에 외래키를 두자

### 엔티티 클래스 개발
**가급적 Getter는 열어두고, Setter는 꼭 필요한 경우에만 사용하자**
- Getter는 아무리 호출해도 호출 하는 것 만으로 어떤 일이 발생하지는 않는다. 하지만 Setter를 호출하면 데이터가 변한다. 
- 따라서 Setter를 막 열어두면 엔티티가 왜 변경되는지 추적하기 점점 힘들어진다.
- 생성 이후에 변경할 필요가 없는데 setter가 외부에 노출되어 있으면 이것을 사용하는 다른 개발자들은 필드를 변경해도 된다고 생각하게 된다.
- 그래서 엔티티를 변경할 때는 Setter 대신에 변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공하는 것이 좋다.

**모든 연관관계는 지연로딩(LAZY)으로 설정하자**
- 즉시로딩(EAGER)은 연관된 데이터를 다 가져오기 때문에 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 
특히 JPQL을 실행할 때 N+1 문제가 자주 발생한다.
- 연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
- @XToOne(OneToOne, ManyToOne) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.

**컬렉션은 필드에서 초기화 하자**
- null 문제에서 안전하다.
- 하이버네이트는 엔티티를 영속화 할 때, 컬랙션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다. 
  만약 어떤 메서드에서 컬렉션을 바꿔버리면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다. 
  따라서 필드레벨에서 생성하는 것이 가장 안전하고, 코드도 간결하다.
  
**스프링 부트 테이블, 필드명 설정 (엔티티(필드) -> 테이블(컬럼))**
1. 카멜 케이스 -> 언더스코어(memberPoint -> member_point)
2. . -> _
3. 대문자 -> 소문자

## 애플리케이션 구현
레포지토리 계층 개발 -> 서비스 계층 개발 -> 테스트 -> 웹 계층 개발

@PersistenceContext: EntityManager 주입.
스프링 데이터 JPA를 사용하면 @Autowired로 대체 가능

@Transactional: 트랜잭션, 영속성 컨텍스트. 
JPA를 이용한 데이터 변경은 트랜잭션 안에서 일어나야 함
- readOnly=true: 데이터의 변경이 없는 읽기 전용 메서드에 사용.
  영속성 컨텍스트를 플러시 하지 않으므로 약간의 성능 향상
  
\* 테스트 케이스에서 @Transactional을 사용하면 각각의 테스트를 실행할 때마다 트랜잭션을 시작하고, 
테스트가 끝나면 트랜잭션을 강제로 롤백   
-> persist는 commit되는 시점에 DB에 반영되기 때문에 insert 쿼리가 실행되지 않음 

> 실무에서는 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서 회원 테이블의 회원명 컬럼에 유니크 제약 조건을 추가하는 것이 안전하다.

통합테스트 -> DB를 실행하고 돌려야 함   
테스트는 케이스 격리된 환경에서 실행하고, 끝나면 데이터를 초기화하는 것이 좋음   
-> 메모리 DB 사용    
-> 테스트용 설정 파일 생성(test/resources/application.yml)   
테스트에서 스프링을 실행하면 이 위치에 있는 설정 파일을 읽음
(만약 이 위치에 없으면 src/resources/application.yml을 읽음)

스프링 부트는 datasource 설정이 없으면, 기본적으로 메모리 DB를 사용하고, driver-class도 현재 등록된
라이브러리를 보고 찾아줌   
추가로 ddl-auto도 create-drop 모드로 동작    
=> 데이터소스나, JPA 관련된 별도의 추가 설정을 하지 않아도 됨

**도메인 모델 패턴**   
엔티티가 비즈니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것   
도메인 주도 설계에서는 엔티티 자체가 해결할 수 있는 비즈니스 로직(데이터 값 조작)은 엔티티 내부에 작성하는 것이 응집도를 늘리고 관리하기 좋음      
데이터 조작을 외부에서 하려면 setter가 필요한데 엔티티 내부에서 하게 되면 setter를 사용하지 않아도 됨 

### 상품 엔티티 개발
```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // 비즈니스 로직
    /**
     * 재고수량 증가
     */
    public void addStock(int quantity) {
        stockQuantity += quantity;
    }

    /**
     * 재고수량 감소
     */
    public void removeStock(int quantity) {
        int resStock = stockQuantity - quantity;
        if (resStock < 0) {
            throw new NotEnoughStockException("Not enough stock");
        }
        stockQuantity = resStock;
    }
}
```

객체 생성 방법
1. 생성자
2. 생성 메서드(정적 팩토리 메서드)
3. Builder

복잡한 엔티티의 경우 단순히 new를 사용하는 방법보다는 생성 메서드를 사용하는 것이 메서드 이름을 통해 생성 의도를 나타낼 수 있기 때문에 더 좋음   
-> 디폴트 생성자를 private로 설정(JPA 사용 시 protected)   
단순한 엔티티라면 자바가 기본으로 제공하는 new를 사용

### 검색 기능 개발
JPA에서의 **동적쿼리**

**JPQL**
```java
@Repository
public class OrderRepository {
    
  public List<Order> findAll(OrderSearch orderSearch) {
    String jpql = "select o From Order o join o.member m";
    boolean isFirstCondition = true;

    //주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
      if (isFirstCondition) {
        jpql += " where";
        isFirstCondition = false;
      } else {
        jpql += " and";
      }
      jpql += " o.status = :status";
    }
    
    //회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      if (isFirstCondition) {
        jpql += " where";
        isFirstCondition = false;
      } else {
        jpql += " and";
      }
      jpql += " m.name like :name";
    }

    TypedQuery<Order> query = em.createQuery(jpql, Order.class)
            .setFirstResult(10) // 10번째 요소부터
            .setMaxResults(1000); // 최대 1000건

    if (orderSearch.getOrderStatus() != null) {
      query = query.setParameter("status", orderSearch.getOrderStatus());
    }
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      query = query.setParameter("name", orderSearch.getMemberName());
    }
    return query.getResultList();
  }
}
```
JPQL 쿼리를 문자로 생성하기는 번거롭고, 실수로 인한 버그가 발생하기 쉬움

**JPA Criteria**
```java
@Repository
public class OrderRepository {
    
  public List<Order> findAllByCriteria(OrderSearch orderSearch) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Order> cq = cb.createQuery(Order.class);
    Root<Order> o = cq.from(Order.class);
    Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
    
    List<Predicate> criteria = new ArrayList<>();
    
    //주문 상태 검색
    if (orderSearch.getOrderStatus() != null) {
      Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
      criteria.add(status);
    }
    
    //회원 이름 검색
    if (StringUtils.hasText(orderSearch.getMemberName())) {
      Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
      criteria.add(name);
    }
    
    cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
    TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
    return query.getResultList();
  }
}
```
JPA Criteria는 JPA 표준 스펙으로, JPQL을 자바 코드로 작성할 수 있게 해줌   
But, 실무에서 사용하기에 너무 복잡하고 가독성이 떨어져 유지보수하기 어려움

**Querydsl**   
쿼리를 자바 코드로 작성   
문법 오류를 컴파일 시점에 알 수 있음

### 웹 계층 개발
부트스트랩에서 css, js 파일 가져올 시 적용이 안되면 reload(synchronize), rebuild 해줌

스프링 부트 타임리프 viewName 매핑   
-> resources:templates/ + {ViewName} + .html

값을 뷰에 전달하기 위해 스프링 MVC가 제공하는 Model 객체에 보관

```java
@Getter @Setter // Setter를 써줘야 값이 들어감
public class MemberForm {

    // 필수 필드
    @NotEmpty(message = "회원 이름은 필수입니다.")
    private String name;
  
    // 선택 필드
    private String city;
    private String street;
    private String zipcode;
}
```
```java
@Controller
@RequiredArgsConstructor
public class MemberController {

    @PostMapping("/members/new")
    // Validation 기능을 사용한다고 명시
    // 오류 처리
    public String create(@Valid MemberForm form, BindingResult result) {
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        // 리다이렉트
        return "redirect:/";
    }
}
```
- @Valid: @NotEmpty와 같은 validation 기능 사용 명시   
- BindingResult: 이것을 사용하지 않으면 에러 발생 시 white label page로 넘어가지만, 
사용 시 변수에 에러 내용이 담겨 처리 가능
  
**Validation 위치**   
1. HTTP 요청 파라미터에 대한 부분은 컨트롤러에서 최대한 검증 (Ex. price >= 0)
2. 내부 DB 조회나 외부 호출이 필요한 검증들은 서비스에서 검증 (Ex. stockQuantity >= 0)
3. 해당 엔티티가 가지고 있는 데이터 만으로 모두 검증할 수 있는 경우는 엔티티에서 검증

**Form 사용 이유**   
Entity를 그대로 사용하면 화면을 처리하기 위한 기능이 추가됨   
-> 코드가 지저분해짐   
-> 유지보수하기 어려워짐   
=> 별도의 Form 객체나 DTO(Data Transform Object) 사용(Form은 controller 계층에서만 사용)

\* DTO: 데이터를 전송하기 위한 getter, setter만 있는 객체

Get 데이터를 뿌릴 때에도 entity를 그대로 뿌리기 보다는 DTO로 변환해서 화면에 필요한 데이터만 뿌리는 것이 좋음    
** API의 경우에는 절대 entity를 반환하면 안됨!!   
-> 로직을 추가하면 스펙이 변하기 때문

![스크린샷 2021-10-13 오후 6 15 10](https://user-images.githubusercontent.com/68456385/137104520-d37e302a-7907-4ea8-8a50-eb1c9d8f93a0.png)

### 변경 감지와 병합(merge)
**준영속 엔티티**   
영속성 컨텍스트가 더는 관리하지 않는 엔티티   
임의로 만들어낸 엔티티 중 이미 DB에 한번 저장되어서 식별자가 존재하는 엔티티도 포함

준영속 엔티티를 수정하는 2가지 방법   
1. 변경 감지 기능
2. 병합(merge)

**변경 감지(Dirty Checking)**   
```java
@Transactional
void update(Item itemParam) { // itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
    Item findItem = em.find(Item.class, itemParam.getId()); // 영속 엔티티를 조회
    findItem.setPrice(itemParam.getPrice()); // 데이터를 수정(트랜잭션)
}
```
준영속 엔티티의 식별자를 이용하여 영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법   
트랜잭션이 끝나고 커밋 시점에 JPA가 flush를 날려 변경사항을 찾고 쿼리를 생성해서 DB에 반영   
save() 호출 필요 x

**병합(merge)**   
```java
@Transactional
void update(Item itemParam) { // itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
    Item mergeItem = em.merge(itemParam);
}
```
준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능   
준영속 엔티티의 식별자 값으로 영속 엔티티를 조회한 후, 영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체   
트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행   

![image](https://user-images.githubusercontent.com/68456385/131481174-8f7a87c5-916d-4633-bfd9-e293bcce50d3.png)
1. merge()를 실행한다.
2. 파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.   
   2-1. 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장한다.
3. 조회한 영속 엔티티(mergeMember)에 member 엔티티의 값을 채워 넣는다. 이때 member 엔티티의 모든 값
   을 mergeMember에 밀어 넣는다. mergeMember의 “회원1”이라는 이름이 “회원명변경”으로 바뀐다.
4. 영속 상태인 mergeMember를 반환한다.

변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이 변경되고, 
병합시 값이 없으면 null로 업데이트가 됨      
=> 엔티티를 변경할 때는 변경 감지를 사용

- 컨트롤러에서 되도록이면 엔티티를 생성하지 말자
- 파라미터 또는 dto를 이용하여 트랜잭션이 있는 서비스 계층에 식별자와 변경할 데이터를 전달
- 트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고(트랜잭션 내에서만 조회 가능), 엔티티의 데이터를 직접 변경
- 데이터 변경 시 setter를 사용하면 변경 지점을 파악하기 어렵기 때문에 엔티티에 의미있는 메서드를 만들어 변경

## API 개발
### 프로젝트 분리
예외처리와 같이 공통으로 처리할 때 대부분 패키지 단위로 적용   
API와 화면은 공통 처리할 요소가 다름(JSON, HTML)

### 회원 등록 API
```java
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/members")
    public CreateMemberResponse saveMember(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    @AllArgsConstructor
    private static class CreateMemberResponse {
        private Long id;
    }
}
```
- @RestController: @Controller + @ResposeBody   
- @RequestBody: JSON으로 온 body를 파라미터에 매핑
```java
@Entity
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty(message = "회원 이름은 필수입니다.")
    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
```
화면 계층의 검증 로직이 엔티티에 들어가면 좋지 않음   
- API에 따라 적용 검증이 달라질 수 있음 -> 하나의 엔티티에 여러 API 요청사항을 담기 어려움
- 엔티티가 바뀌면 API 스펙이 바뀜

=> API 스펙을 위한 별도의 DTO를 생성해서 사용

**API를 만들 때는 엔티티를 파라미터로 사용하지 말고, 외부에 노출하지 말자**

```java
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/members")
    public CreateMemberResponse saveMember(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    @AllArgsConstructor
    private static class CreateMemberResponse {
        private Long id;
    }

    @Data
    private static class CreateMemberRequest {
        @NotEmpty(message = "회원 이름은 필수입니다.")
        private String name;
    }
}
```

### 회원 조회 API
응답으로 엔티티를 반환하면 좋지 않음
- 엔티티의 모든 값이 노출됨
- 응답 스펙을 맞추기 위해 로직이 추가됨(Ex. @JsonIgnore)
- 하나의 엔티티에 여러 API를 위한 응답 로직을 담기 어려움
- 엔티티가 변경되면 API 스펙이 변함
- 컬렉션을 직접 반환하면 항후 API 스펙을 변경하기 어려움(객체가 아닌 컬렉션에 담기기 때문에 클라이언트에서 요구하는 필드를 추가할 수 없음)

=> API 응답 스펙에 맞추어 별도의 DTO를 반환
=> 별도의 Result 클래스를 생성해서 컬렉션을 감싼 후 반환

```java
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    
    @GetMapping("/api/members")
    public Result memberList() {
        List<Member> members = memberService.findMembers();
        // 엔티티 -> DTO 변환
        List<MemberDto> collect = members.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect, collect.size());
    }

    @Data
    @AllArgsConstructor
    private static class Result<T> {
        private T data;
        private int count; // 컬렉션이 아닌 객체이기 때문에 원하는 필드를 추가할 수 있음
    }

    @Data
    @AllArgsConstructor
    private static class MemberDto {
        private String name;
    }
}
```

# JPA
출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-%ED%99%9C%EC%9A%A9-1

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
    트랜잭션이 커밋되는 시점에(flush) 엔티티와 스냅샷을 비교하여 변경사항이 있으면 update SQL을 생성하여 반영 
   
   
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
\* 즉시 로딩: Join으로 연관된 객체까지 한번에 조회   
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
Entity Manager는 요청이 올 때마다 생성

**JPA의 모든 데이터 변경은 트랜잭션안에서 실행되어야 함**

**JPQL**   
SQL을 추상화한 객체 지향 쿼리 언어   
테이블이 아닌 엔티티를 대상으로 쿼리

## 영속성 관리
### 영속성 컨텍스트
엔티티를 영구 저장하는 환경   
Entity Manager를 통해 접근   
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

- 변경 감지
- 쓰기 지연 SQL 저장소의 쿼리를 DB에 전송

## 엔티티 매핑
### 객체와 테이블 매핑
**@Entity**   
JPA가 관리하는 엔티티   
테이블과 매핑할 클래스   
기본 생성자 필요(public 또는 protected) -> 리플렉션   
final, enum, interface, inner 클래스 사용 x

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
  주로 MySQL에서 사용
- SEQUENCE: DB 시퀀스 오브젝트 사용. 시퀀스 오브젝트에서 값을 가져와 기본 키에 저장. @SequenceGenerator 필요   
  주로 Oracle에서 사용
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
- toString(): 구현 x
- JSON 생성 라이브러리: Controller에서 엔티티를 직접 반환 x

웬만하면 단방향 연관관계로 설계하고 비즈니스적으로 필요한 곳에만 양방향 연관관계로 설정
(Ex. Order - OrderItem)

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

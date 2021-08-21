# Spring JPA
출처: https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81%EB%B6%80%ED%8A%B8-JPA-%ED%99%9C%EC%9A%A9-1

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

package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // 테이블 이름 지정
@Getter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // 외래키 이름 지정
    private Member member;

    // orderItems에 값을 넣고 Order를 저장하면 OrderItem이 자동으로 생성됨(생성 외 모든 동작에도 적용)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // Date를 사용하면 어노테이션을 매핑시켜줘야 함
    // LocalDateTime - Java 8 문법, Hibernate에서 자동으로 지원
    private LocalDateTime orderDate;

    // ORDINAL - 값으로 숫자가 들어감, STRING - 문자열
    // ORDINAL은 새로운 열거형 값이 중간에 추가될 때 문제 발생
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    // 연관관계 편의 메서드 - 하나의 메서드로 양방향 연관관계 모두 처리
    // 핵심적으로 컨트롤하는 쪽에 생성하는 것이 좋음
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }
}

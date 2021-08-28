package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // 테이블 이름 지정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 디폴트 생성자를 protected로 설정
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // 외래키 이름 지정
    private Member member;

    // cascade - orderItems에 값을 넣고 Order를 저장하면 OrderItem이 자동으로 생성됨(생성 외 모든 동작에도 적용)
    // OrderItem과 같이 Order에서만 사용하는 종속적인 엔티티에 부여 -> OrderItem의 생명주기는 Order에 달려 있게 됨
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

    // 생성 메서드(정적 팩토리 메서드)
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.status = OrderStatus.ORDER;
        order.orderDate = LocalDateTime.now();
        return order;
    }

    // 비즈니스 로직
    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException();
        }

        this.status = OrderStatus.CANCEL;
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
//        return orderItems.stream()
//                .mapToInt(OrderItem::getTotalPrice)
//                .sum();
    }
}

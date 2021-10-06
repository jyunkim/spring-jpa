package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter @Setter
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "member_id") // 테이블은 관례상 테이블명_id를 많이 사용
    private Long id;

    private String name;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    @Embedded
    private Address address;

    @ElementCollection
    @CollectionTable(name = "address",
            joinColumns = @JoinColumn(name = "member_id"))
    private List<Address> addressHistory = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "favorite_food",
            joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "food_name")
    private Set<String> favoriteFoods = new HashSet<>();

    @OneToMany(mappedBy = "member") // 연관관계 주인이 아님을 명시. Order 엔티티의 member 필드에 의해 매핑
    private List<Order> orders = new ArrayList<>(); // 컬렉션은 필드에서 초기화하는 것이 null 문제에서 안전
}

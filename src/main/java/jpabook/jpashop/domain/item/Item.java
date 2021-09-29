package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.BaseEntity;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.dto.UpdateBookDto;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 상속관계 전략 지정. SINGLE_TABLE - 한 테이블에 모두 저장
@DiscriminatorColumn(name = "dtype") // 구분 칼럼
@Getter
public abstract class Item extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    public void update(UpdateBookDto itemDto) {
        this.name = itemDto.getName();
        this.price = itemDto.getPrice();
        this.stockQuantity = itemDto.getStockQuantity();
    }

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

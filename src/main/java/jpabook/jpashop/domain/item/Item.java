package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@BatchSize(size = 100) // // V3.1 컬렉션이 아닐 경우 적용할 때
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // joined는 가장 정교화된 스타일, singleTable은 한 테이블에 다 넣는것
@DiscriminatorColumn(name = "dtype")
@Getter @Setter
public abstract class Item { // 구현체를 가지고 갈 것이기 때문에
    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    //==비즈니스 로직==// 데이터를 가지고 있는 쪽에 비즈니스 메소드가 있는게 좋음!
    /** stock 증가 */
    public void addStock(int quantity) { // 재고 수량 더해주기
        this.stockQuantity += quantity;
    }
    /** stock 감소 */
    public void removeStock(int quantity)   {
        int restStock = this.stockQuantity - quantity;
        if(restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}

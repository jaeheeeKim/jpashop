package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 누군가 생성자를 생성하더라도 하면 안되는걸 알아챌듯
public class Order {
    @Id @GeneratedValue
    @Column(name="order_id")
    private Long id;
                            // 지연로딩⭐
    @ManyToOne(fetch = LAZY) // 여러 주문에 한 멤버 연관관계
    @JoinColumn(name = "member_id")// foreign key 매핑을 무엇으로 할건지
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) // order만 orderItem을 사용하기때문에 cascade 가능함
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL) // order만 delivery를 사용하기때문에 cascade 가능함
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // 주문 상태 [ORDER, CANCEL]

    //==연관관계 메소드==//핵심적으로 컨트롤하는 쪽이 갖고있는게 좋음!
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }
//    public static void main(String[] args) {
//        Member member = new Member();
//        Order order = new Order();
//
//        member.getOrders().add(order);
//        order.setMember(member);
//    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메소드==// 생성하는 지점 변경해야 할 때 createOrder만 바꿔주면 됨
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 로직==//
    /** 주문 취소 */
    public void cancel() {
        if(delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송 완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for(OrderItem orderItem : orderItems) { // 루프를 돌면서 재고를 원상복귀
             orderItem.cancel();
        }
    }

    //==조회 로직==//
    /** 전체 주문 가격 조회 */
    public int getTotalPrice() {
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }
}

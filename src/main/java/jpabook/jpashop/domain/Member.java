package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;
    @Embedded// 내장 타입을 포함했다는 뜻
    private Address address;

    @OneToMany(mappedBy = "member") // 한 멤버에 여러 주문 연관관계
    // 연관관계의 주인이 아닐때, member는 매핑을 하는게 아니라 매핑된 거울일 뿐이며 읽기 전용이 됨
    private List<Order> orders = new ArrayList<>();
}

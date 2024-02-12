package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable // 내장 타입을 포함했다는 뜻
@Getter @Setter
public class Address {
    private String city;
    private String street;
    private String zipcode;

    protected Address() { // @NoArgsConstructor
    }

    public Address(String city, String street, String zipcode) { // @AllArgsConstructor
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}

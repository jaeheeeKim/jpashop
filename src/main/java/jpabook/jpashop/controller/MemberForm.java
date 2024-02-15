package jpabook.jpashop.controller;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberForm {

    @NotEmpty(message = "회원 이름은 필수입니다.") // hasError로 랜더링됨
    private String name;

    private String city;
    private String street;
    private String zipcode;
}

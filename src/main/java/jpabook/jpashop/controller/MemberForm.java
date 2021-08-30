package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter // Setter를 써줘야 값이 들어감
public class MemberForm {

    // 필수 필드
    @NotEmpty(message = "회원 이름은 필수입니다.")
    private String name;

    // 선택 필드
    private String city;
    private String street;
    private String zipcode;
}

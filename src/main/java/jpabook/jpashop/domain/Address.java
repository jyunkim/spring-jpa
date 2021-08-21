package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable // 내장 타입
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    // 값 타입은 변경 불가능하게 설계해야 함
    // 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스를 만듬
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

    // JPA에서 기본 생성자를 사용하기 때문에 기본 생성자 필요(public 또는 protected)
    // public으로 두는 것 보다는 protected로 설정하는 것이 그나마 더 안전
    protected Address() {
    }
}

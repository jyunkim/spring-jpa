package jpabook.jpashop.dto;

import lombok.Getter;

@Getter
public class MemberDto {

    private Long id;
    private String name;

    public MemberDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

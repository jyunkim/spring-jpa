package jpabook.jpashop.dto;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Team;
import lombok.Getter;

@Getter
public class MemberDto {

    private Long id;
    private String name;
    private Team team;

    public MemberDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public MemberDto(Member member) {
        id = member.getId();
        name = member.getName();
        team = member.getTeam();
    }
}

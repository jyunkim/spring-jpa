package jpabook.jpashop.dto;

import com.querydsl.core.annotations.QueryProjection;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Team;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {

    private Long id;
    private String name;
    private int age;
    private Team team;

    @QueryProjection
    public MemberDto(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public MemberDto(Member member) {
        id = member.getId();
        name = member.getName();
        age = member.getAge();
        team = member.getTeam();
    }
}

package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();
}

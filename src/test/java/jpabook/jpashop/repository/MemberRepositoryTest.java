package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.dto.MemberDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @Test
    void basicCRUD() {
        Member member1 = new Member();
        member1.setName("member1");
        memberRepository.save(member1);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        assertThat(findMember1).isEqualTo(member1);

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(1);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(1);

        memberRepository.delete(member1);

        long remainCount = memberRepository.count();
        assertThat(remainCount).isEqualTo(0);
    }

    @Test
    void findByName() {
        Member m1 = new Member();
        m1.setName("member1");
        memberRepository.save(m1);

        List<Member> result = memberRepository.findByName("member1");

        assertThat(result.get(0).getName()).isEqualTo("member1");
    }

    @Test
    void findUser() {
        Member m1 = new Member();
        m1.setName("member1");
        m1.setAge(22);
        memberRepository.save(m1);

        List<Member> result = memberRepository.findUser("member1", 22);

        assertThat(result.get(0).getName()).isEqualTo("member1");
        assertThat(result.get(0).getAge()).isEqualTo(22);
    }

    @Test
    void findMemberDto() {
        Member m1 = new Member();
        m1.setName("member1");
        m1.setAge(22);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();

        for (MemberDto dto : memberDto) {
            System.out.println(dto.getId() + " " + dto.getName());
        }
    }

    @Test
    void findByNames() {
        Member m1 = new Member();
        m1.setName("member1");
        m1.setAge(22);
        memberRepository.save(m1);

        List<Member> members = memberRepository.findByNames(Arrays.asList("member1", "member2"));

        for (Member member : members) {
            System.out.println(member.getId() + " " + member.getName());
        }
    }

    @Test
    public void paging() {
        Member m1 = new Member();
        m1.setName("member1");
        m1.setAge(10);
        memberRepository.save(m1);

        Member m2 = new Member();
        m2.setName("member2");
        m2.setAge(10);
        memberRepository.save(m2);

        Member m3 = new Member();
        m3.setName("member3");
        m3.setAge(10);
        memberRepository.save(m3);

        Member m4 = new Member();
        m4.setName("member4");
        m4.setAge(10);
        memberRepository.save(m4);

        Member m5 = new Member();
        m5.setName("member5");
        m5.setAge(10);
        memberRepository.save(m5);

        // name을 기준으로 내림차순 정렬된 크기가 3인 페이지의 첫번째 페이지 요청
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "name"));

        Page<Member> page = memberRepository.findPageByAge(10, pageRequest);

        List<Member> members = page.getContent();

        assertThat(members.size()).isEqualTo(3); // 조회된 데이터 수
        assertThat(members.get(0).getName()).isEqualTo("member5");
        assertThat(page.getTotalElements()).isEqualTo(5); // 전체 데이터 수
        assertThat(page.getTotalPages()).isEqualTo(2); // 전체 페이지 수
        assertThat(page.getNumber()).isEqualTo(0); // 현재 페이지 번호
        assertThat(page.getNumberOfElements()).isEqualTo(3); // 현재 페이지의 데이터 수
        assertThat(page.isFirst()).isTrue(); // 첫번째 페이지인지
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는지

        Slice<Member> slice = memberRepository.findSliceByAge(10, pageRequest);

        members = slice.getContent();

        assertThat(members.size()).isEqualTo(3);
//        assertThat(slice.getTotalElements()).isEqualTo(5);
        assertThat(slice.getNumber()).isEqualTo(0);
//        assertThat(slice.getTotalPages()).isEqualTo(2);
        assertThat(slice.isFirst()).isTrue();
        assertThat(slice.hasNext()).isTrue();

        // 페이지를 유지하면서 엔티티를 DTO로 변환
        Page<MemberDto> dtoPage = page.map(member -> new MemberDto(member.getId(), member.getName()));
    }
}

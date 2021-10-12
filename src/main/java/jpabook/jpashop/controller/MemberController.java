package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.dto.MemberDto;
import jpabook.jpashop.dto.MemberSearchCondition;
import jpabook.jpashop.dto.MemberTeamDto;
import jpabook.jpashop.repository.MemberJpaRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MemberJpaRepository memberJpaRepository;

    @GetMapping("/members/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    // Validation 기능을 사용한다고 명시
    // 에러 처리
    public String create(@Valid MemberForm form, BindingResult result) {
        // 에러 발생 시 view에 에러를 담아서 보냄
        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        // 리다이렉트
        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }

    @GetMapping("/members/page")
    public Page<MemberDto> pageList(Pageable pageable) {
        Page<Member> page = memberRepository.findAll(pageable);
//        return page.map(member -> new MemberDto(member));
        // 메소드 참조: 람다 표현식이 하나의 메소드만을 호출하는 경우 매개변수를 제거하고 사용 가능
        // 생성자 참조 시 new 사용
        return page.map(MemberDto::new);
    }

    @GetMapping("/members/search/v1")
    @ResponseBody
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.search(condition);
    }
}

package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController // @Controller + @ResposeBody
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // @RequestBody: JSON으로 온 body를 파라미터에 매핑
    @PostMapping("/api/members")
    public CreateMemberResponse saveMember(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // PUT - 전체 업데이트
    // PATCH - 부분 업데이트
    @PatchMapping("/api/members/{id}")
    public UpdateMemberResponse updateMember(@PathVariable("id") Long id,
                                             @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        // memberService.update()에서 member를 반환해도 되지만, command(수정)와 query(조회)를 분리 -> 유지보수하기 좋음
        Member member = memberService.findOne(id);
        return new UpdateMemberResponse(member.getId(), member.getName());
    }

    @GetMapping("/api/members")
    public Result memberList() {
        List<Member> members = memberService.findMembers();
        // 엔티티 -> DTO 변환
        List<MemberDto> collect = members.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect, collect.size());
    }

    @Data // Lombok - 기본 메서드 생성
    @AllArgsConstructor
    private static class CreateMemberResponse {
        private Long id;
    }

    @Data
    private static class CreateMemberRequest {
        @NotEmpty(message = "회원 이름은 필수입니다.")
        private String name;
    }

    @Data
    @AllArgsConstructor
    private static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    private static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    private static class Result<T> {
        private T data;
        private int count; // 컬렉션이 아닌 객체이기 때문에 원하는 필드를 추가할 수 있음
    }

    @Data
    @AllArgsConstructor
    private static class MemberDto {
        private String name;
    }
}

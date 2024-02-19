package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController // @ResponseBody(데이터 자체를 바로 json이나 xml로 보낼때 씀)+@Controller
@RequiredArgsConstructor
public class MemberApiController { // 🚫참고: 실무에서는 엔티티(Member)를 API 스펙에 노출하면 안된다!🚫
    private final MemberService memberService;

    // 1번의 장점은 CreateMemberResponse 클래스 같은걸 안만들어도 된다는 것
    /**
     * 등록 V1: 요청 값으로 Member 엔티티를 직접 받는다.
     * 문제점
     * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
     * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등등)
     * - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한
     모든 요청 요구사항을 담기는 어렵다.
     * - 엔티티가 변경되면 API 스펙이 변한다.
     * 결론
     * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
     */
    @PostMapping("/api/v1/members")                 // validation(검증) 자동으로 됨 // Member 엔티티와 API는 1대1로 매핑이 되어있음!
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    @Data
    static class CreateMemberResponse {
        private Long id; // alt+insert constructor (아래 생성자 만들어줌)

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    // 2번의 장점은 setter(예를 들어 setName을 setUsename으로)로만 수정해주면 되니까, entity를 변경해도 API 스펙 바뀌지 않는다는 것이 장점
    /**
     * 등록 V2: 요청 값으로 Member 엔티티 대신에 별도의 DTO(CreateMemberRequest)를 받는다.
     *
     * `CreateMemberRequest` 를 `Member` 엔티티 대신에 RequestBody와 매핑한다.
     * 엔티티와 프레젠테이션 계층을 위한 로직을 분리할 수 있다.
     * 엔티티와 API 스펙을 명확하게 분리할 수 있다.
     * 엔티티가 변해도 API 스펙이 변하지 않는다.
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }
    @Data
    static class CreateMemberRequest {
        private String name;
    }
}

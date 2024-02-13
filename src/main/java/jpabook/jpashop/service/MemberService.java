package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) // readOnly=true 조회 성능 최적화(읽기 전용이니까 리소스 많이 쓰지말고)!
@RequiredArgsConstructor // final이 있는 필드만 생성자 만들어줌
public class MemberService {
//    @Autowired // 변경이 불가능한 단점이 있음!
    private final MemberRepository memberRepository; // final 적어주면 컴파일 시점 체크가 가능함

    // 스프링이 뜰 때 생성자에서 injection을 해줌, 장점은 테스트케이스 작성할 때 주입해야할거를 안놓치고
    // 생성시점에 어떤 값이 필요해(의존하고 있어)라고 명확하게 알 수 있음
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

//    @Autowired // Setter Injection 메소드를 통해 주입하면 되는데 누군가 setter를 바꾸는 위험이 있음
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

    // 회원가입
    @Transactional // 데이터 변경하는건 꼭 트랜젝션이 있어야함!⭐ public 메소드들은 다 걸림
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        //EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }
}

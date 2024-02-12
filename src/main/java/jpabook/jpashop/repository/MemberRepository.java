package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // Component 스캔의 대상이 되어서 스프링 빈이 등록해줌
public class MemberRepository {
    @PersistenceContext // 엔티티 매니저를 주입해줌
    private EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id)  {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        // ctrl+alt+n                  member entity객체에 대한 alias를 m으로 주고 엔티티 멤버를 조회하라는 qlString임
        return em.createQuery("select m from Member m", Member.class) // ctrl+alt+v
                .getResultList();
    }

    public List<Member> findByName(String name) { // jpqlString 대상이 테이블이 아닌 Member 엔티티
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
        .getResultList();
    }
}

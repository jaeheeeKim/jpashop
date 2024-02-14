package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
    private final EntityManager em;

    public void save(Item item) { // alt+shift+enter 는 import
        if(item.getId() == null) { // 아이디가 없으면 새로 persist 호출하고
            em.persist(item);
        } else { // 아이디가 있으면 jpa를 통해서 DB에 들어가는
            em.merge(item); // 강제 업데이트
        }
    }
    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }
    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}

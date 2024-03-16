package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController { /** 지연 로딩과 간단 조회 성능 최적화 **/

    private final OrderRepository orderRepository;

    // 1️⃣이렇게 하면 안됨 어떤 문제가 발생하는지 보여 주기 위함 ➡️ 무한루프 빠짐! Member에 가니까 orders가 있고, Order에 가니까 member가 있어서ㅋㅋㅋ
    // ➡️ 두번째 문제는 500 에러 발생! 지연로딩(LAZY)의 Member를 DB에서 안가져오고 프록시 객체를 가짜로 넣어놓고
    // 뭔가 멤버의 객체 값을 꺼내거나 손대면 그때 DB에서 멤버 객체를 가져와서 채워준다
    // 2️⃣ ➡️ 해결 방법은! hibernate5Module(*지연로딩 무시함*) 설치해서 Bean 등록 해주면 해결~!
    // 🚫성능 문제! 나는 member랑 deliery만 필요한데 orderItems나 이런 데이터 다 노출이 되어버리고 어마어마하게 많은 query들이 나가버림
    // 결과적으로 간단한 주문 조회를 위해 엔티티를 직접 노출하는건 굉장히 안좋음
    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch()); // 1️⃣
        for(Order order : all) { // ➡️ LAZY 로딩 초기화 하면 원하는대로 만들어짐 그러나... API를 만들 때 이렇게 복잡하게 만들지 않음!!! 그리고 쓸데없이 데이터를 다 노출해서 운영할 때 좋지 않음
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }

    // v1이랑 v2 모두 문제는!🚫 데이터베이스 쿼리가 너무 많이 호출 된다는 점!

    // 실무에서 JPA 성능 문제는 90% 다 N+1 문제 때문에 터짐!!!⭐ 따라서 패치 조인을 완벽하게 이해하면 대부분의 성능 문제는 해결이 됨!!!
    // v3 진짜 객체를 채워서 다 가져옴 (JPA에만 있는 join fetch 문법)
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        // ORDER 2개 있다면,
        // N + 1 -> 1 + 회원 N + 배송 N
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
        /*List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;*/
    }

    // v2와 v3의 결과는 완전히 같지만, v2는 쿼리문 5개 날리고, v3가 쿼리문 하나로 날려줘서 성능 문제를 해결함
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {
        return orderRepository.findAllWithMemberDelivery().stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    //- v3에서는 엔티티를 조회한 후 이 엔티티를 DTO로 변환함 ⭐따라서 비즈니스 로직을 써서 데이터를 변경 가능함
    //- v4는 JPA에서 DTO를 바로 끄집어냄 ⭐따라서 DTO로 조회한 것은 아예 변경할 수 없음
    // v3와 비교를 했을 때 v4는 원하는 값만 select문으로 가져옴! 그런데 v4가 화면에는 최적화 되었는데 거의 재사용성이 없음(로직을 재활용 할 수 없음)!
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderRepository.findOrderDtos(); // repository는 가급적이면 순수한 entity를 조회할 때 사용해야한다. v3까지는 잘 했는데 Dto를 조회하는건 화면에 박히는 느낌임!
    }

    /** 쿼리 방식 선택 권장 순서
     * 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다. <- V2
     * 2. 필요하면 페치 조인으로 성능을 최적화한다. 대부분의 성능 이슈가 해결된다. <- V3
     * 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다. <- V4
     * 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다.
     */

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }
}

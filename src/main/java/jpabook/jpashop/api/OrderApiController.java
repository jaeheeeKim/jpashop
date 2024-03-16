package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController { /** 컬렉션 조회 최적화 **/
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * 주문 조회 V1: 엔티티 직접 노출
     **/
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch()); // 검색 조건 없이 다 가져 오는 것
        for (Order order : all) { // iter 단축키
            // Lazy 로딩을 호출해서 정상적으로 프록시가 초기화된 데이터가 로딩된 것들만 api로 반환됨
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화

            List<OrderItem> orderItems = order.getOrderItems(); // Lazy 강제 초기화
            orderItems.stream().forEach(o -> o.getItem().getName()); // 아래 iterator를 람다식으로 전환!
//            for (OrderItem orderItem : orderItems) { // iterator 단축키
//                orderItem.getItem().getName(); // Lazy 강제 초기화
//            }
        }
        return all;
    }

    /**
     * 주문 조회 V2: 엔티티를 DTO로 변환 (쿼리문 너무 많이 나감)
     **/
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(OrderDto::new)
                .collect(toList()); // alt+enter 해서 import

        return result;
    }

    /**
     * 주문 조회 V3: 엔티티를 DTO로 변환 - 페치 조인 최적화 (쿼리문 하나만 나감)
     * 치명적인 단점 : 페이징 쿼리가 아예 안나감 🚫
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() { // v3 문제는 order가 1개, orderItems가 2개라면 똑같은 order가 2번 조회 되어버림
        return orderRepository.findAllWithItem().stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * 주문 조회 V3.1: 엔티티를 DTO로 변환 - 페이징과 한계 돌파
     * 그니까 V3는 페이징 안되는 단점🚫이 있는 대신에 패치조인으로 쿼리문이 한번에 나가고,
     * V3.1은 쿼리문이 한번에 나가지는 못하지만 조금 나가면서 페이징&성능 최적화 가능,
     * 🚫단점은 DB에서 application으로 중복 데이터를 많이 전송하게 됨
     *
     * 김영한 강사님은 이 방식을 굉장히 선호하심!!!😃😃
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        return orderRepository.findAllWithMemberDelivery(offset, limit).stream() // toOne 관계 걸린 경우 다 가져오라
                .map(OrderDto::new)
                .collect(toList());
    }

    /**
     * 주문 조회 V4 : JPA에서 DTO 직접 조회
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }
    /**
     * 주문 조회 V5 : JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }
    /**
     * 주문 조회 V6 : JPA에서 DTO 직접 조회 - 플랫 데이터 최적화
     * 가장 큰 장점 : 쿼리가 한 번만 나감!!!⭐ 근데 페이징은 못함...
     * order(데이터 중복으로 가져오기 때문에)를 기준으로 페이징을 하면 안되고, orderFlatDto나 orderItems로 페이징을 하는건 가능하겠다
     */
    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> ordersV6() {
        return orderQueryRepository.findAllByDto_flat();
    }

    @Getter // 없으면 프로퍼티 오류남
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems; // DTO 안에 Entity가 있으면 외부에 노출 되기 때문에 안된다!🚫 Entity에 대한 의존을 완전히 끊어야함!

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            order.getOrderItems().stream().forEach(o -> o.getItem().getName()); // orderItems가 엔티티이기 때문에 null이 나오므로 이렇게 적어줘야함
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList()).reversed();
        }
    }

    @Getter
    static class OrderItemDto { // Entity가 직접 노출되는 위험이 있기 때문에 orderItems까지 다 DTO로 변환 해줘야함!
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }

    }



}

package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/order")
    public String createForm(Model model) {
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        model.addAttribute("members", members);
        model.addAttribute("items", items);

        return "order/orderForm";
    }

    @PostMapping("/order")
    public String order(@RequestParam("memberId") Long memberId, // html의 name값 넘어옴
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count) {
        orderService.order(memberId, itemId, count); // JPA는 트랜잭션 안에서 가장 깔끔하게 동작함 ➡️ service단에서 로직을 짜주는 이유
        // service계층에서 엔티티에 더 의존하고 안에서 찾으면 할 수 있는게 더 많아짐! 엔티티는 영속 상태로 흘러 가기 때문에 깔끔해짐
        return "redirect:/orders";
    }
    // 이렇게 안하고 바로 repository로 바로 만들어도됨!
    @GetMapping("/orders")   //@ModelAttribute에 세팅해두면 model박스에 자동으로 담긴다
    public String orderList(@ModelAttribute("orderSearch")OrderSearch orderSearch, Model model) {
        List<Order> orders = orderService.findOrders(orderSearch);
        // model.addAttribute("orderSearch", orderSearch); @ModelAttribute는 이 부분이 생략된거라고 보면 됨!
        model.addAttribute("orders", orders);

        return "order/orderList";
    }

    @PostMapping("/order/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
}

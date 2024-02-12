package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    @GetMapping("hello") // 주소 url
    public String hello(Model model) { // 스프링UI에 있는 model이란 어떤 데이터를 실어서 view에 넘길 수 있음
        model.addAttribute("data", "hello!!");
        return "hello"; // resources/templates/ 내 view(화면) 이름
    }
}

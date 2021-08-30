package jpabook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

//    Logger log = LoggerFactory.getLogger(getClass()); // 애노테이션으로 대체

    @GetMapping("/")
    public String home() {
        log.info("Home controller"); // 실행 시 로그 남김
        return "home";
    }
}

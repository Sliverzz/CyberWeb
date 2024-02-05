package com.sean.cyberweb.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/site")
public class IndexController {

    // 首頁
    @GetMapping("/index")
    public String home() {
        return "/pages/site/index";
    }
}

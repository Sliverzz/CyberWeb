package com.sean.cyberweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String home() {
        return "pages/index/home";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "pages/index/dashboard";
    }
}

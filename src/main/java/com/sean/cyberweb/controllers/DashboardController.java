package com.sean.cyberweb.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    // 後台首頁
    @GetMapping("/")
    public String dashboard() {
        return "pages/dashboard/index";
    }

    // 後台登入
    @GetMapping("/login")
    public String login(){
        return "pages/dashboard/login";
    }

    // 後台註冊
    @GetMapping("/signUp")
    public String signUp(){
        return "pages/dashboard/signUp";
    }

    // 商品管理
    @GetMapping("/product")
    public String header() {
        return "pages/dashboard/product";
    }
}

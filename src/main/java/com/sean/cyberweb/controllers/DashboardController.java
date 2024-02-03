package com.sean.cyberweb.controllers;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // 使用者管理
    @GetMapping("/user")
    public String user(){
        return "pages/dashboard/user";
    }

    // 商品管理
    @GetMapping("/product")
    public String product() {
        return "pages/dashboard/product";
    }

    // 訂單管理
    @GetMapping("/order")
    public String order(){
        return "pages/dashboard/order";
    }
}

package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;

    // 使用構造函數注入 UserService
    @Autowired //spring boot 4.3以上可省略
    public DashboardController (UserService userService){
        this.userService = userService;
    }

    // 後台登入
    @GetMapping("/login")
    public String login(){
        return "/pages/dashboard/login";
    }

    // 後台註冊
    @GetMapping("/signUp")
    public String signUp(){
        return "/pages/dashboard/signUp";
    }

    // 後台首頁
    @GetMapping("/index")
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "/pages/dashboard/index";
    }

    // 使用者管理
    @GetMapping("/profile")
    public String profile(Model model){
        User currentUser = userService.getCurrentUser();

        // 获取用户头像逻辑
        String avatarUrl = "/assets/img/profileImg/profile-img.png"; // 預設頭向
        if (currentUser.hasAvatar()) {
            avatarUrl = currentUser.getProfileImagePath(); // 用戶頭像URL，使用 profileImagePath 属性
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("avatarUrl", avatarUrl); // 將頭像URL添加到模型

        return "/pages/dashboard/profile";
    }

    // 商品管理
    @GetMapping("/product")
    public String product(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "/pages/dashboard/product";
    }

    // 訂單管理
    @GetMapping("/order")
    public String order(Model model){
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);
        return "/pages/dashboard/order";
    }
}

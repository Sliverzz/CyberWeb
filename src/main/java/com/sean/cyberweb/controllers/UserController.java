package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 使用構造函數注入 UserService
    @Autowired //spring boot 4.3以上可省略
    public UserController (UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signUp")
    public ModelAndView signUp(@ModelAttribute User user) {
        ModelAndView modelAndView = new ModelAndView();

        // 註冊
        boolean isRegistered = userService.registerUser(user);

        if (isRegistered) {
            // 註冊成功
            modelAndView.setViewName("pages/dashboard/login"); // 跳轉到登入頁面
            modelAndView.addObject("message", "Registration successful!");
        } else {
            // 註冊失敗
            modelAndView.setViewName("pages/dashboard/signUp"); // 回註冊頁面顯示錯誤訊息
            modelAndView.addObject("message", "Registration failed. Please try again.");
        }

        return modelAndView;
    }
}

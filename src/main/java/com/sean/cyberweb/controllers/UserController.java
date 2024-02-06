package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 使用構造函數注入 UserService
    @Autowired //spring boot 4.3以上可省略
    public UserController (UserService userService){
        this.userService = userService;
    }

    // 註冊
    @PostMapping("/signUp")
    public String signUp(@ModelAttribute User user, RedirectAttributes redirectAttributes) {

        boolean isRegistered = userService.registerUser(user);

        if (isRegistered) {
            // 註冊成功，傳遞成功消息
            redirectAttributes.addFlashAttribute("flashMessageType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Registration successful!");
            return "redirect:/dashboard/login"; // 重定向到登入頁面
        } else {
            // 註冊失敗，傳遞失敗消息
            redirectAttributes.addFlashAttribute("flashMessageType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Registration failed. Please try again.");
            return "redirect:/dashboard/signUp"; // 重定向回註冊頁面
        }
    }

    // 檢查帳號、email是否重複
    @PostMapping("/ajaxCheckExistence")
    @ResponseBody
    public Map<String, Boolean> ajaxCheckExistence(@RequestBody Map<String, String> data) {
        Map<String, Boolean> response = new HashMap<>();
        if (data.containsKey("username")) {
            // 檢查帳號是否存在
            boolean exists = userService.usernameExists(data.get("username"));
            response.put("exists", exists);
        } else if (data.containsKey("email")) {
            // 檢查email是否存在
            boolean exists = userService.emailExists(data.get("email"));
            response.put("exists", exists);
        }
        return response;
    }

    // 後台使用者資料
    @PostMapping("/profile")
    public String updateUserProfile(User user, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserProfile(user);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
            return "redirect:/dashboard/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Update failed: " + e.getMessage());
            return "redirect:/dashboard/profile";
        }
    }
}

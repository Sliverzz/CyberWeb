package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.services.UserService;
import com.sean.cyberweb.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Autowired
    public UserController (UserService userService){
        this.userService = userService;
    }

    // 登入頁
    @GetMapping("/login")
    public String login(){
        return "/pages/user/login";
    }

    // 註冊頁
    @GetMapping("/signUp")
    public String signUp(){
        return "/pages/user/signUp";
    }

    // 註冊
    @PostMapping("/signUp")
    public String signUp(@ModelAttribute User user, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        boolean isRegistered = userService.registerUser(user);

        if (isRegistered) {
            WebUtils.addFlashMessage(redirectAttributes, "success", "Registration successful!");
            return "redirect:/user/login"; // 重定向到登入頁面
        } else {
            WebUtils.addFlashMessage(redirectAttributes, "error", "Registration failed. Please try again.");
            return "redirect:/user/signUp"; // 重定向回註冊頁面
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

    // 更新用使用者資料
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping("/updateProfile")
    public String updateUserProfile(@RequestParam(value = "userHashId", required = false) String userHashId, User user,
                                    HttpServletRequest request,RedirectAttributes redirectAttributes) {
        try {
            // 解碼hashId
            Long userId = userService.decode(userHashId);
            if (userId == null) {
                throw new IllegalArgumentException("Invalid user identifier.");
            }
            // 確保更新時是正確id
            user.setId(userId);

            userService.updateUserProfile(user);

            // flashMessage與重導
            WebUtils.addFlashMessage(redirectAttributes, "success", "Profile updated successfully!");
            return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);

        } catch (Exception e) {
            // flashMessage與重導
            WebUtils.addFlashMessage(redirectAttributes, "error", "Update failed: " + e.getMessage());
            return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);
        }
    }
}

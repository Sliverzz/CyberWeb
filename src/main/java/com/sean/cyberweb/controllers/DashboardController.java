package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.services.ProductService;
import com.sean.cyberweb.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserService userService;

    @Autowired
    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    // 首頁
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/index")
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentUser();

        // hashId處理
        String userHashId = userService.encode(currentUser.getId());

        model.addAttribute("userHashId", userHashId);
        model.addAttribute("user", currentUser);
        return "pages/dashboard/index";
    }

    // 使用者管理
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profile")
    public String profile(Model model) {
        User currentUser = userService.getCurrentUser();

        // hashId處理
        String userHashId = userService.encode(currentUser.getId());

        // 預設頭像的Web路徑
        String avatarUrl = currentUser.getAvatarUrl();
        // 檢查用戶是否有設定頭像
        if (currentUser.hasAvatar() && currentUser.getProfileImagePath() != null) {
            // 直接使用用戶設定的頭像路徑
            avatarUrl = currentUser.getProfileImagePath();
        }

        model.addAttribute("userHashId", userHashId);
        model.addAttribute("user", currentUser);
        model.addAttribute("avatarUrl", avatarUrl); // 將頭像URL添加到模型

        return "pages/dashboard/profile";
    }

    // 產品
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/product")
    public String product(Model model) {
        User currentUser = userService.getCurrentUser();

        String userHashId = userService.encode(currentUser.getId());
        String productImagePath = "assets/img/no-image.jpg";

        model.addAttribute("userHashId", userHashId);
        model.addAttribute("user", currentUser);
        model.addAttribute("productImagePath", productImagePath);

        return "pages/dashboard/product";
    }

    // 訂單管理
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/order")
    public String order(Model model){
        User currentUser = userService.getCurrentUser();

        // hashId處理
        String userHashId = userService.encode(currentUser.getId());

        model.addAttribute("userHashId", userHashId);
        model.addAttribute("user", currentUser);
        return "pages/dashboard/order";
    }
}

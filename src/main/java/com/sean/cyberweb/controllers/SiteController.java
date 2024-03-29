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
@RequestMapping("/site")
public class SiteController {

    private final UserService userService;

    @Autowired
    public SiteController(UserService userService) {
        this.userService = userService;
    }

    // 首頁
    @GetMapping("/index")
    public String home(Model model) {
        User currentUser = userService.getCurrentUser();

        /* 由於首頁與產品頁結合的原因，
         * 產品加入購物車時需要抓取當前currentUser的hashId才能導入對應用戶的購物車，
         * 故此處加入currentUser檢查以便未登入也能進到首頁，
         * 其餘頁面的controller皆已在config中擋掉所有 "ROLE_USER、ROLE_ADMIN" 以外的權限用戶
         */
        if (currentUser != null) {
            String userHashId = userService.encode(currentUser.getId());
            model.addAttribute("userHashId", userHashId);
            model.addAttribute("user", currentUser);
        }
        return "pages/site/index";
    }

    // 購物車
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/cart")
    public String cart(Model model) {
        User currentUser = userService.getCurrentUser();

        // hashId處理
        String userHashId = userService.encode(currentUser.getId());

        model.addAttribute("userHashId", userHashId);
        model.addAttribute("user", currentUser);
        return "pages/site/cart";
    }

    // 會員個人資料管理
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
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

        return "pages/site/profile";
    }

    // 個人訂單管理
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/order")
    public String order(Model model){
        User currentUser = userService.getCurrentUser();

        // hashId處理
        String userHashId = userService.encode(currentUser.getId());

        model.addAttribute("userHashId", userHashId);
        model.addAttribute("user", currentUser);
        return "pages/site/order";
    }
}

package com.sean.cyberweb.controllers;

import com.sean.cyberweb.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/img")
public class ImgController {

    private final UserService userService;

    // 使用構造函數注入 UserService
    @Autowired //spring boot 4.3以上可省略
    public ImgController (UserService userService){
        this.userService = userService;
    }

    // 上傳圖片
    @PostMapping("/upload")
    public String uploadImage(@RequestParam("userHashId") String userHashId, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            // 如果沒有上傳新圖片就不進行更新
            redirectAttributes.addFlashAttribute("flashMessageType", "info");
            redirectAttributes.addFlashAttribute("flashMessage", "No new image was uploaded.");
            return "redirect:/dashboard/profile";
        }

        try {
            // 解碼
            Long userId = userService.decode(userHashId);
            if (userId == null) {
                // 如果解析失敗，則返回錯誤
                redirectAttributes.addFlashAttribute("flashMessageType", "error");
                redirectAttributes.addFlashAttribute("flashMessage", "Invalid user identifier.");
                return "redirect:/dashboard/profile";
            }

            // 根據解析後的用戶ID更新用戶頭像
            userService.updateUserProfileImage(userId, file);
            redirectAttributes.addFlashAttribute("flashMessageType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Image uploaded successfully.");
            return "redirect:/dashboard/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("flashMessageType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Image upload failed." + e.getMessage());
            return "redirect:/dashboard/profile";
        }
    }
}

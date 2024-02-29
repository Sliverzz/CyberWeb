package com.sean.cyberweb.controllers;

import com.sean.cyberweb.services.UserService;
import com.sean.cyberweb.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/img")
public class ImgController {

    private final UserService userService;

    @Autowired
    public ImgController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("userHashId") String userHashId, @RequestParam("file") MultipartFile file,
                              HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            WebUtils.addFlashMessage(redirectAttributes, "info", "No new image was uploaded.");
            return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);
        }

        try {
            Long userId = userService.decode(userHashId);
            if (userId == null) {
                WebUtils.addFlashMessage(redirectAttributes, "error", "Invalid user identifier.");
                return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);
            }

            userService.updateUserProfileImage(userId, file);
            WebUtils.addFlashMessage(redirectAttributes, "success", "Image uploaded successfully.");
            return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);
        } catch (Exception e) {
            WebUtils.addFlashMessage(redirectAttributes, "error", "Image upload failed." + e.getMessage());
            return WebUtils.redirectBack(request, WebUtils.DEFAULT_REDIRECT);
        }
    }
}

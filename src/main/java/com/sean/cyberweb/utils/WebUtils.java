package com.sean.cyberweb.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class WebUtils {

    // 自定義預設重導路徑
    public static final String DEFAULT_REDIRECT = "/pages/site/index";

    // 新增FlashMessage訊息
    public static void addFlashMessage(RedirectAttributes redirectAttributes, String type, String message) {
        redirectAttributes.addFlashAttribute("flashMessageType", type);
        redirectAttributes.addFlashAttribute("flashMessage", message);
    }

    // 自動返回原頁面
    public static String redirectBack(HttpServletRequest request, String defaultRedirect) {
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : defaultRedirect);
    }
}

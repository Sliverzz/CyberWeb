package com.sean.cyberweb.controllers;

import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/img")
public class ImgController {

    private final UserService userService;

    // 使用構造函數注入 UserService
    @Autowired //spring boot 4.3以上可省略
    public ImgController (UserService userService){
        this.userService = userService;
    }

    @GetMapping("/image/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) {
        // 圖片文件的絕對路徑
        String imagePath = "C:\\Users\\ML2\\IdeaProjects\\CyberWeb\\src\\main\\resources\\static\\assets\\img\\profileImg" + imageName;

        try {
            // 讀取圖片文件內容
            File file = new File(imagePath);
            FileInputStream inputStream = new FileInputStream(file);
            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            inputStream.close();

            // 設置header告訴瀏覽器返回的是圖片
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);

            // 返回圖片
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/upload")
    public String uploadUImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes){
        if (file.isEmpty()){
            redirectAttributes.addFlashAttribute("flashMessageType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "Registration successful!");
            return "redirect:/dashboard/profile";
        }

        // 設置保存路徑
        String imagePath = "C:\\Users\\ML2\\IdeaProjects\\CyberWeb\\src\\main\\resources\\static\\assets\\img\\profileImg";
        File imageDir = new File(imagePath);
        if(!imageDir.exists()){
            imageDir.mkdirs();
        }
        try {
            // 保存圖片文件
            String fileName = file.getOriginalFilename();
            String filePath = imagePath + File.separator + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);

            // 將文件路徑保存到數據庫中
            User currentUser = userService.getCurrentUser();
            currentUser.setProfileImagePath(imagePath + fileName);
            userService.updateUserProfile(currentUser); // 更新用戶信息

            redirectAttributes.addFlashAttribute("flashMessageType", "success");
            redirectAttributes.addFlashAttribute("flashMessage", "successful!");
            return "redirect:/dashboard/profile";

        }catch (IOException e){
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("flashMessageType", "error");
            redirectAttributes.addFlashAttribute("flashMessage", "Image saving failed");
            return "redirect:/dashboard/profile";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

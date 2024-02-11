package com.sean.cyberweb.services;

import com.sean.cyberweb.domain.Role;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.repository.UserRepository;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final String UPLOAD_DIR = "C:\\Users\\ML2\\Desktop\\Cyber Web\\Img\\avatar";
    private final Hashids hashids;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.hashids = new Hashids("sean_salt_2024", 8);
    }

    // 註冊
    @Transactional
    public boolean registerUser(User user) {
        // 雖說ajax已經檢查過，這邊二次檢查再次確保使用者名稱或電子郵件是否已被註冊
        if (userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail())) {
            // 已存在返回false
            return false;
        }

        // 前端傳過來的role值為空就寫入ROLE_USER
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole(Role.ROLE_USER);
        }

        // 加密使用者密碼
        user.setPassword(encryptPassword(user.getPassword()));

        userRepository.save(user);

        // 註冊成功
        return true;
    }

    // 密碼加密
    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    // 註冊頁-檢查帳號是否存在
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    // 註冊頁-檢查email是否存在
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // 更新後台使用者資料
    @Transactional
    public void updateUserProfile(User updatedUserDetails) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = ((UserDetails)authentication.getPrincipal()).getUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new Exception("User not found with username: " + currentUsername));

        currentUser.setFirstName(updatedUserDetails.getFirstName());
        currentUser.setLastName(updatedUserDetails.getLastName());
        currentUser.setEmail(updatedUserDetails.getEmail());
        currentUser.setPhoneNumber(updatedUserDetails.getPhoneNumber());

        userRepository.save(currentUser);
    }

    // 獲取當前用戶
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails)principal).getUsername();
            return userRepository.findByUsername(username)
                    .orElse(null); // 更安全地處理空值
        }
        return null;
    }

    // 上傳用戶頭像
    public void updateUserProfileImage(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // 為了避免文件名衝突，可以加上當前時間戳
        Path path = Paths.get(UPLOAD_DIR).resolve(fileName); // 確保使用resolve來連接路徑
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING); // 使用copy替代write來處理文件流

        // 根據 ResourceHandler 設置的訪問路徑，給前端使用
        String webPath = "/profileImg/" + fileName;
        user.setProfileImagePath(webPath);
        userRepository.save(user); // 更新用户记录
    }

    // hashId
    public String encode(Long id) {
        return hashids.encode(id);
    }

    public Long decode(String hash) {
        long[] ids = hashids.decode(hash);
        return ids.length > 0 ? ids[0] : null;
    }
}

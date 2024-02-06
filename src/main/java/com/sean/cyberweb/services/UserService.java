package com.sean.cyberweb.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // 使用構造函數注入 UserRepository
    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 註冊
    @Transactional
    public boolean registerUser(User user) {
        // 雖說ajax已經檢查過，這邊二次檢查再次確保使用者名稱或電子郵件是否已被註冊
        if (userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail())) {
            // 已存在返回false
            return false;
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
        currentUser.setProfileImagePath(updatedUserDetails.getProfileImagePath());

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
}

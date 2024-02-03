package com.sean.cyberweb.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sean.cyberweb.domain.User;
import com.sean.cyberweb.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    // 使用構造函數注入 UserRepository
    @Autowired //spring boot 4.3以上可省略
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 註冊
    @Transactional
    public boolean registerUser(User user) {
        // 檢查使用者名稱或電子郵件是否已被註冊
        if (userRepository.existsByUsername(user.getUsername()) || userRepository.existsByEmail(user.getEmail())) {
            // 如果使用者名稱或電子郵件已存在，返回註冊失敗
            return false;
        }

        // 加密使用者密碼，確保安全性
        user.setPassword(encryptPassword(user.getPassword()));

        // 保存使用者到數據庫
        userRepository.save(user);

        // 註冊成功
        return true;
    }

    // 密碼加密
    private String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    // 檢查帳號
    public boolean usernameExists(String username) {
        // 檢查用戶名是否已存在於數據庫中
        return userRepository.existsByUsername(username);
    }

    // 檢查email
    public boolean emailExists(String email) {
        // 檢查電子郵件是否已存在於數據庫中
        return userRepository.existsByEmail(email);
    }
}

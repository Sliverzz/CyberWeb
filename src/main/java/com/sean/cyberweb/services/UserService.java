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
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
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
        // 只有註冊會有密碼加密行為，所以寫成局部變量
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
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
}

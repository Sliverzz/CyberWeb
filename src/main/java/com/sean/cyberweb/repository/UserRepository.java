package com.sean.cyberweb.repository;

import com.sean.cyberweb.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // 找帳號
    User findByUsername(String username);
    
    // 找Email
    User findByEmail(String email);
    
    // 檢查帳號是否存在
    boolean existsByUsername(String username);
    
    // 檢查Email是否存在
    boolean existsByEmail(String email);
}

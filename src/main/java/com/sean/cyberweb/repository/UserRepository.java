package com.sean.cyberweb.repository;

import com.sean.cyberweb.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 找帳號
    Optional<User> findByUsername(String username);

    // 檢查帳號是否存在
    boolean existsByUsername(String username);
    
    // 檢查Email是否存在
    boolean existsByEmail(String email);
}

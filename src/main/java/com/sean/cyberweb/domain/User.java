package com.sean.cyberweb.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName; // 名

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName; // 姓

    @Column(length = 20)
    private String phoneNumber; // 手機

    @Column(nullable = false)
    private Boolean status = true; // 帳戶是否啟用

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated = LocalDateTime.now(); // 帳戶創建時間

    //    暫時不需要role，需要再開。
    //    @Column(name = "role", nullable = false)
    //    private String role;

    @Column(name = "profile_image_path")
    private String profileImagePath;

    public boolean hasAvatar() {
        return profileImagePath != null && !profileImagePath.isEmpty();
    }

    public String getAvatarUrl() {
        if (hasAvatar()) {
            return profileImagePath;
        } else {
            return "/assets/img/profileImg/profile-img.png";
        }
    }

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated; // 帳戶最後更新時間
}

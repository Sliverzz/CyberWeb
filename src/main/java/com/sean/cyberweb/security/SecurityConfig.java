package com.sean.cyberweb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.Objects;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // 密碼加密
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security 登入/登出 filter
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionSecurityContextRepository httpSSCRepo = new HttpSessionSecurityContextRepository();
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用csrf
                .securityContext((context)-> context.securityContextRepository(httpSSCRepo)) // 儲存登入狀態在session
                .sessionManagement(session -> session
                        .maximumSessions(1) // 同帳號最大登入數
                        .maxSessionsPreventsLogin(true)
                        .sessionRegistry(sessionRegistry()) // 使用自定義的sessionRegistry
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // (重要)統一在controller端控管權限
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler())
                )
                .formLogin(form -> form
                        .loginPage("/user/login") // 自定義登入頁面
                        .loginProcessingUrl("/login") // 登入url
                        .defaultSuccessUrl("/site/index", true) // 登入成功後的導向
                        .permitAll() // 允許所有用戶訪問登入頁面
                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true) // 確保清除認證
                        .logoutSuccessUrl("/user/login") // 登出成功後的導向頁面
                        .addLogoutHandler(customLogoutHandler(sessionRegistry())) // 確保SecurityContext被清除
                );
        return http.build();
    }

    // session管理器
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // 自定義登出處理器
    @Bean
    public LogoutHandler customLogoutHandler(SessionRegistry sessionRegistry) {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            if (authentication != null) {
                String sessionId = request.getSession().getId();
                sessionRegistry.removeSessionInformation(sessionId);
            }
        };
    }

    // 自定義403錯誤處理器
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException e) -> {
            // 使用HTTP頭部的Referer值來重定向回上一頁
            String refererUrl = request.getHeader("Referer");
            // 如果Referer頭部不存在，重定向到默認頁面或首頁
            response.sendRedirect(Objects.requireNonNullElse(refererUrl, "/site/index"));
        };
    }
}

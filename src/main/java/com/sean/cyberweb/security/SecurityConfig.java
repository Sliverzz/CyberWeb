package com.sean.cyberweb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableWebSecurity
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
                        .requestMatchers("/dashboard/login", "/dashboard/signUp").permitAll() // 允許訪問登入和註冊頁面(先這樣寫測試因為@PreAuthorize失效)
                        .requestMatchers("/dashboard/**").authenticated() // "/dashboard/**"下的所有請求都需要驗證
                        .anyRequest().permitAll() // 其他所有請求允許訪問
                )
                .formLogin(form -> form
                        .loginPage("/dashboard/login") // 自定義登入頁面
                        .loginProcessingUrl("/login") //登入url
                        .defaultSuccessUrl("/dashboard/index", true) // 登入成功後的導向
                        .permitAll() // 允許所有用戶訪問登入頁面
                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true) // 確保清除認證
                        .logoutSuccessUrl("/dashboard/login")
                        .addLogoutHandler(customLogoutHandler(sessionRegistry())) // 確保SecurityContext被清除
                );
        return http.build();
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

    // session管理器
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}

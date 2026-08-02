package com.codegym.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Khai báo thuật toán mã hóa mật khẩu cực mạnh: BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Thiết lập trạm gác SecurityFilterChain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Cho phép tất cả mọi người được truy cập vào Trang chủ, Đăng nhập, Đăng ký và kho ảnh
                        .requestMatchers("/", "/register", "/login", "/search", "/product/**", "/api/**", "/images/**", "/css/**").permitAll()

                        // Vùng cấm: Bắt buộc phải có quyền ADMIN mới được chui vào link có chữ /admin/...
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Các đường link còn lại (nếu có) phải đăng nhập mới được xem
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // Chỉ định file giao diện Đăng Nhập mà lát nữa ta sẽ tự làm
                        .loginPage("/login")
                        .defaultSuccessUrl("/") // Đăng nhập xong đẩy ra trang chủ
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // Đăng xuất xong đẩy ra trang chủ
                        .permitAll()
                );
        return http.build();
    }
}

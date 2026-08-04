package com.codegym.store.controller;

import com.codegym.store.dto.UserRegisterDTO;
import com.codegym.store.model.Role;
import com.codegym.store.model.User;
import com.codegym.store.repository.RoleRepository;
import com.codegym.store.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;

@Controller
public class AuthController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Hiển thị Form Đăng Ký
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegisterDTO());
        return "user/register";
    }

    // 2. Xử lý Đăng Ký
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegisterDTO dto,
                               BindingResult bindingResult) {
        // Check trùng Username
        if (userRepository.existsByUsername(dto.getUsername())) {
            bindingResult.rejectValue("username", "error.user", "Tên đăng nhập này đã được sử dụng");
        }
        // Check trùng Số điện thoại
        if (userRepository.existsByPhone(dto.getPhone())) {
            bindingResult.rejectValue("phone", "error.user", "Số điện thoại này đã được đăng ký");
        }
        // Check trùng Email
        if (userRepository.existsByEmail(dto.getEmail())) {
            bindingResult.rejectValue("email", "error.user", "Email này đã được đăng ký");
        }
        // Nếu có bất kỳ lỗi nào (Regex hoặc lỗi Trùng dữ liệu) -> Trả về trang Đăng ký
        if (bindingResult.hasErrors()) {
            return "user/register";
        }

        // Tạo User mới
        User user = new User();
        user.setUsername(dto.getUsername());

        // MÃ HÓA MẬT KHẨU BẰNG BCRYPT TẠI ĐÂY
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        // Gán quyền ROLE_USER mặc định
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("ROLE_USER");
            return roleRepository.save(role);
        });
        user.setRoles(Collections.singleton(userRole));
        // Lưu vào DB
        userRepository.save(user);
        return "redirect:/login?success";
    }

    // 3. Hiển thị Form Đăng Nhập
    @GetMapping("/login")
    public String showLoginForm() {
        return "user/login";
    }
}

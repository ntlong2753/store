package com.codegym.store.config;

import com.codegym.store.model.Role;
import com.codegym.store.model.User;
import com.codegym.store.repository.RoleRepository;
import com.codegym.store.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Inject các Repository và Máy băm mật khẩu vào
    public DatabaseInitializer(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Kiểm tra xem quyền ROLE_ADMIN đã có trong bảng Role chưa, nếu chưa thì tạo
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role role = new Role();
            role.setName("ROLE_ADMIN");
            return roleRepository.save(role);
        });

        // 2. Kiểm tra xem tài khoản "admin" đã tồn tại chưa
        // Dùng findByUsername.isEmpty() cho an toàn, tránh lỗi thiếu hàm
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            // MẬT KHẨU MẶC ĐỊNH LÀ 123456 (Đã tự động được băm bằng BCrypt)
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setFullName("Quản Trị Viên");
            admin.setEmail("admin@nexgenpc.com");
            admin.setPhone("0999999999");

            // Ép quyền ADMIN cho tài khoản này
            admin.setRoles(Collections.singleton(adminRole));

            // Lưu xuống Database
            userRepository.save(admin);

            System.out.println("==================================================");
            System.out.println(" ĐÃ TỰ ĐỘNG TẠO TÀI KHOẢN ADMIN!");
            System.out.println(" Username: admin");
            System.out.println(" Password: 123456");
            System.out.println("==================================================");
        }
    }
}

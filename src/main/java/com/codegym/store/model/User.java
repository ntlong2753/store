package com.codegym.store.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên đăng nhập (duy nhất)
    @Column(nullable = false, unique = true)
    private String username;

    // Mật khẩu (sẽ được mã hóa băm BCrypt)
    @Column(nullable = false)
    private String password;

    // Họ và tên đầy đủ
    @Column(nullable = false)
    private String fullName;

    // Email (duy nhất)
    @Column(nullable = false, unique = true)
    private String email;

    // Số điện thoại (duy nhất)
    @Column(nullable = false, unique = true)
    private String phone;

    // Quan hệ N-N với bảng roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // Ảnh đại diện của người dùng (nối sang bảng UserAvatar)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "avatar_id", referencedColumnName = "id")
    private UserAvatar userAvatar;


}

package com.codegym.store.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_avatar")
@Data
public class UserAvatar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Đường dẫn ảnh
    @Column(nullable = false)
    private String path;
}

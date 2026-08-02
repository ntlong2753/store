package com.codegym.store.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_address")
@Data
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String receiverName; // Tên người nhận

    @Column(nullable = false)
    private String phone; // Số điện thoại nhận hàng

    @Column(nullable = false, length = 500)
    private String fullAddress; // Địa chỉ chi tiết (Thôn/Xóm, Phường, Quận...)

    @Column(nullable = false)
    private boolean isDefault = false; // Cờ đánh dấu địa chỉ mặc định

    // Khóa ngoại liên kết N-1 với bảng User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

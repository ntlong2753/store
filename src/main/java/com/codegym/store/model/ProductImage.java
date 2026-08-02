package com.codegym.store.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "product_image")
@Data
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lưu chuỗi URL (Ví dụ: /images/anh_cpu.jpg)
    private String path;

    // Liên kết với bảng Product (Nhiều ảnh thuộc về 1 Sản phẩm)
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}

package com.codegym.store.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// ... các import cũ giữ nguyên

@Entity
@Table(name = "product")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá sản phẩm không được âm")
    private BigDecimal price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stock;

    /*(Ghi chú: cascade = CascadeType.ALL nghĩa là khi bạn lưu CPU, nó tự lưu luôn danh sách ảnh.
    orphanRemoval = true nghĩa là khi bạn xóa 1 ảnh khỏi danh sách, nó tự động xóa ảnh đó dưới Database).*/
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

}

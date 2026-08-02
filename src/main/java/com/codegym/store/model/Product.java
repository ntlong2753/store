package com.codegym.store.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

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

    private String imagePath;
}

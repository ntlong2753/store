package com.codegym.store.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Giỏ hàng thuộc về duy nhất 1 User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 1 Giỏ hàng có thể chứa nhiều Mặt hàng (Tự động lưu và tự động xóa mồ côi)
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    // Hàm tự động tính tổng tiền của cả giỏ hàng
    public java.math.BigDecimal getTotalPrice() {
        return items.stream()
                .map(item -> item.getProduct().getPrice().multiply(new java.math.BigDecimal(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

}

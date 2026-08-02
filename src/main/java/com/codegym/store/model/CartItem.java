package com.codegym.store.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cart_item")
@Data
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nằm trong giỏ hàng nào?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Móc thẳng vào bảng Product (Cha của CPU, VGA...)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Số lượng của sản phẩm này (Ví dụ: mua 200 chiếc CPU)
    @Column(nullable = false)
    private int quantity;
}

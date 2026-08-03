package com.codegym.store.model;

public enum OrderStatus {
    PENDING("Chờ xác nhận"),
    APPROVED("Đã duyệt"),
    SHIPPING("Đang giao"),
    DELIVERED("Đã nhận hàng"),
    REJECTED("Không duyệt");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

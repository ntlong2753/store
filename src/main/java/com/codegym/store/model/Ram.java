package com.codegym.store.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "ram")
@Data
@EqualsAndHashCode(callSuper = true)
public class Ram extends Product {
    private String brand; // Hãng
    private String capacity; // Dung lượng (VD: 8GB, 16GB, 32GB)
    private String ramStandard; // Chuẩn RAM (DDR4, DDR5)
}

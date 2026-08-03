package com.codegym.store.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "vga")
@Data
@EqualsAndHashCode(callSuper = true)
public class Vga extends Product {
    private String gpuBrand; // VD: NVIDIA, AMD, Intel
    private String gpuModel; // VD: RTX 4060, RX 7600
    private String cardBrand; // VD: ASUS, Gigabyte, MSI
    private Integer vram; // Dung lượng VRAM (GB)
    private String memoryType; // Loại bộ nhớ (GDDR6, GDDR6X)
    private String series; // VD: GeForce RTX 40 Series, Radeon RX 7000 Series
}

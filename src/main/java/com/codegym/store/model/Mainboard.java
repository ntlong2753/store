package com.codegym.store.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "mainboard")
@Data
@EqualsAndHashCode(callSuper = true)
public class Mainboard extends Product {
    private String brand; // Hãng sản xuất
    private String model; // Mã model
    private String socket; // Socket hỗ trợ
    private String chipset; // Chipset
    private String ramStandard; // Chuẩn RAM
    private String size; // Kích thước (Form Factor)
}

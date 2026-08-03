package com.codegym.store.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "psu")
@Data
@EqualsAndHashCode(callSuper = true)
public class Psu extends Product {

    @NotBlank(message = "Hãng không được để trống")
    private String brand;

    private Integer wattage;

    @NotBlank(message = "Chuẩn nguồn không được để trống")
    private String formFactor;

    @NotBlank(message = "Kích thước không được để trống")
    private String size;
}

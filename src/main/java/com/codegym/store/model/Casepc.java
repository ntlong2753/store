package com.codegym.store.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "casepc")
@Data
@EqualsAndHashCode(callSuper = true)
public class Casepc extends Product {

    @NotBlank(message = "Hãng không được để trống")
    private String brand;

    @NotBlank(message = "Model không được để trống")
    private String modelNumber;

    @NotBlank(message = "Kích thước không được để trống")
    private String formFactor;

    private String supportedMainboard;
}

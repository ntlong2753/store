package com.codegym.store.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "cpu")
@Check(constraints = "cores > 0 AND threads > 0 AND cores <= threads")
@Data
// Của Lombok, để nó lấy luôn cả Getter/Setter của class cha
@EqualsAndHashCode(callSuper = true)
public class Cpu extends Product {
    @NotBlank(message = "Dòng CPU không được để trống")
    private String series; // VD: Core, Ryzen, Xeon

    private String segment; // VD: i5, Ryzen 5, i7
    private String modelNumber; // VD: 12400, 5600X
    private String suffix; // VD: F, K, KF, X

    @Min(value = 1, message = "Số nhân CPU tối thiểu phải là 1")
    private Integer cores;

    @Min(value = 1, message = "Số luồng CPU tối thiểu phải là 1")
    private Integer threads;
    private String socket;
}

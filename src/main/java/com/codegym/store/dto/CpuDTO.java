package com.codegym.store.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) // Nhớ có dòng này để lấy Getter/Setter của cha
public class CpuDTO extends ProductDTO {

    @NotBlank(message = "Dòng CPU không được để trống")
    private String series;

    @Min(value = 1, message = "Số nhân tối thiểu là 1")
    private int cores;

    @Min(value = 1, message = "Số luồng tối thiểu là 1")
    private int threads;

    private String socket;
}

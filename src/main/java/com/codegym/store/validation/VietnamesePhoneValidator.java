package com.codegym.store.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VietnamesePhoneValidator implements ConstraintValidator<VietnamesePhone, String> {

    // Nhóm 1: tiền tố quốc gia - 0 hoặc 84 hoặc +84
    // Nhóm 2: đầu số nhà mạng (đã bỏ số 0 ở đầu)
    private static final String PHONE_REGEX =
            "^(0|84|\\+84)(86|96|97|98|3[2-9]|89|90|93|7[06789]|88|91|94|8[1-5])[0-9]{7}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // Bỏ khoảng trắng, dấu gạch ngang nếu người dùng nhập kiểu "+84 91 234 5678" hoặc "091-234-5678"
        String normalized = value.trim().replaceAll("[\\s-]", "");

        return normalized.matches(PHONE_REGEX);
    }
}

package com.codegym.store.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VietnamesePhoneValidator implements ConstraintValidator<VietnamesePhone, String> {

    private static final String PHONE_REGEX = "^(086|096|097|098|03[2-9]|089|090|093|07[06789]|088|091|094|08[1-5])[0-9]{7}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return value.matches(PHONE_REGEX);
    }
}

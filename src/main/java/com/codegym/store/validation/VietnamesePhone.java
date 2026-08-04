package com.codegym.store.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = VietnamesePhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface VietnamesePhone {
    String message() default "Số điện thoại phải gồm 10 số và thuộc mạng Viettel, MobiFone hoặc VinaPhone";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

package com.codegym.store.dto;

import com.codegym.store.validation.VietnamesePhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 20, message = "Tên đăng nhập phải từ 4 đến 20 ký tự")
    // Dùng Regex để chặn ký tự đặc biệt và khoảng trắng
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tên đăng nhập không được chứa ký tự đặc biệt hoặc khoảng trắng")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @VietnamesePhone
    private String phone;
}

package com.codegym.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AdminController {

    // 1. Controller mở trang Dashboard
    @GetMapping("/admin")
    public String adminDashboard(Model model, Principal principal) {
        // Lấy tên Admin đang đăng nhập để truyền ra màn hình vẫy chào
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        // Trỏ về đúng file dashboard.html mà bạn vừa tạo
        return "admin/dashboard";
    }

    // 2. Controller API cung cấp Ngày Tháng ngầm cho Ajax
    @GetMapping("/api/current-date")
    @ResponseBody
    public Map<String, String> getCurrentDate() {
        // Lấy ngày hiện tại
        LocalDate today = LocalDate.now();

        // Định dạng lại kiểu Việt Nam
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Gói vào JSON gửi về
        Map<String, String> response = new HashMap<>();
        response.put("date", today.format(formatter));

        return response;
    }
}

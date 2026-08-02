package com.codegym.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    // Khi ai đó (có quyền Admin) gõ /admin, tự động đá họ sang trang quản lý CPU
    @GetMapping("/admin")
    public String adminDashboard() {
        return "redirect:/admin/cpu";
    }
}

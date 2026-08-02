package com.codegym.store.controller;

import com.codegym.store.repository.CpuRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class HomeController {

    private final CpuRepository cpuRepository;

    // Tiêm (Inject) CpuRepository vào đây để lấy dữ liệu
    public HomeController(CpuRepository cpuRepository) {
        this.cpuRepository = cpuRepository;
    }

    @GetMapping("/")
    // Thêm @RequestParam để nhận lệnh "viewAll" từ trên URL xuống
    public String home(@RequestParam(defaultValue = "false") boolean viewAll,
                       Model model,
                       Principal principal,
                       HttpServletRequest request) {

        model.addAttribute("cpus", cpuRepository.findAll());
        // Bơm biến cờ viewAll xuống cho giao diện HTML xử lý
        model.addAttribute("viewAll", viewAll);

        // Các code cũ của bạn giữ nguyên
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            boolean isAdmin = request.isUserInRole("ROLE_ADMIN");
            model.addAttribute("isAdmin", isAdmin);
        } else {
            model.addAttribute("isAdmin", false);
        }

        return "store/home";
    }

    // Đầu API này chuyên để phục vụ AJAX gọi ngầm
    @GetMapping("/search")
    public String search(@org.springframework.web.bind.annotation.RequestParam String keyword,
                         Model model,
                         java.security.Principal principal,
                         jakarta.servlet.http.HttpServletRequest request) {

        // Tìm kiếm CPU theo từ khóa
        model.addAttribute("cpus", cpuRepository.findByNameContainingIgnoreCase(keyword));
        // Lúc tìm kiếm thì luôn hiện tất cả kết quả (không giới hạn 5 cái)
        model.addAttribute("viewAll", true);

        // Vẫn check quyền Admin để ẩn/hiện nút "Thêm vào" cho chuẩn
        if (principal != null) {
            model.addAttribute("isAdmin", request.isUserInRole("ROLE_ADMIN"));
        } else {
            model.addAttribute("isAdmin", false);
        }

        // LƯU Ý QUAN TRỌNG:
        // Chỉ trả về ĐÚNG cái mẩu HTML của danh sách sản phẩm (có tên fragment là product-list)
        return "store/product-list";
    }

    // --- GIAI ĐOẠN 1: API MỞ TRANG CHI TIẾT SẢN PHẨM ---
    @GetMapping("/product/{id}")
    public String productDetail(@org.springframework.web.bind.annotation.PathVariable Long id,
                                Model model,
                                java.security.Principal principal,
                                jakarta.servlet.http.HttpServletRequest request) {

        // 1. Tìm CPU trong CSDL dựa vào ID
        com.codegym.store.model.Cpu cpu = cpuRepository.findById(id).orElse(null);
        if (cpu == null) {
            return "redirect:/"; // Nếu gõ link láo không có thật thì đá về trang chủ
        }

        // 2. Gửi CPU xuống cho giao diện hiển thị
        model.addAttribute("cpu", cpu);

        // 3. Vẫn check quyền Admin để biết đường ẩn/hiện nút "Thêm vào giỏ hàng"
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("isAdmin", request.isUserInRole("ROLE_ADMIN"));
        } else {
            model.addAttribute("isAdmin", false);
        }

        return "store/product-detail";
    }

    // --- GIAI ĐOẠN 2: API TÌM KIẾM TRẢ VỀ JSON CHO DROPDOWN ---
    @GetMapping("/api/search")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.List<java.util.Map<String, Object>> searchApi(@org.springframework.web.bind.annotation.RequestParam String keyword) {

        java.util.List<com.codegym.store.model.Cpu> cpus = cpuRepository.findByNameContainingIgnoreCase(keyword);
        java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();

        for (com.codegym.store.model.Cpu cpu : cpus) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", cpu.getId());
            map.put("name", cpu.getName());

            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            map.put("price", df.format(cpu.getPrice()).replace(",", ".") + " ₫");

            if (cpu.getImages() != null && !cpu.getImages().isEmpty()) {
                map.put("image", cpu.getImages().get(0).getPath());
            } else {
                map.put("image", "https://via.placeholder.com/50x50?text=No+Image");
            }
            results.add(map);
        }
        return results;
    }


}

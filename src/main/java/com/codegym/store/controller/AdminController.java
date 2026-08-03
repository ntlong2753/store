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

    private final com.codegym.store.repository.OrderRepository orderRepository;
    private final com.codegym.store.repository.ProductRepository productRepository;
    private final com.codegym.store.repository.UserRepository userRepository;

    public AdminController(com.codegym.store.repository.OrderRepository orderRepository,
                           com.codegym.store.repository.ProductRepository productRepository,
                           com.codegym.store.repository.UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // 1. Controller mở trang Dashboard
    @GetMapping("/admin")
    public String adminDashboard(Model model, Principal principal) {
        // Lấy tên Admin đang đăng nhập để truyền ra màn hình vẫy chào
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        // 1. Doanh thu tháng này
        java.math.BigDecimal revenue = orderRepository.sumRevenueThisMonth();
        model.addAttribute("revenueThisMonth", revenue != null ? revenue : java.math.BigDecimal.ZERO);

        // 2. Đơn hàng mới trong tháng
        Long newOrders = orderRepository.countOrdersThisMonth();
        model.addAttribute("newOrdersCount", newOrders != null ? newOrders : 0L);

        // 3. Tổng linh kiện
        long totalProducts = productRepository.count();
        model.addAttribute("totalProducts", totalProducts);

        // 4. Số lượng khách hàng
        Long customersCount = userRepository.countUsersByRoleUser();
        model.addAttribute("customersCount", customersCount != null ? customersCount : 0L);

        // 5. Đơn hàng gần đây
        java.util.List<com.codegym.store.model.Order> recentOrders = orderRepository.findTop5ByOrderByOrderDateDesc();
        model.addAttribute("recentOrders", recentOrders);

        // 6. Sản phẩm sắp hết hàng (stock <= 5)
        java.util.List<com.codegym.store.model.Product> lowStockProducts = productRepository.findTop5ByStockLessThanEqualOrderByStockAsc(5);
        model.addAttribute("lowStockProducts", lowStockProducts);

        // Trỏ về đúng file dashboard.html
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

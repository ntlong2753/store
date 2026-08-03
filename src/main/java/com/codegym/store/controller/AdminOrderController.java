package com.codegym.store.controller;

import com.codegym.store.model.Order;
import com.codegym.store.model.OrderStatus;
import com.codegym.store.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Danh sách tất cả đơn hàng
    @GetMapping
    public String listAllOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        // Sắp xếp giảm dần theo ID (mới nhất lên đầu)
        orders.sort((o1, o2) -> o2.getId().compareTo(o1.getId()));
        model.addAttribute("orders", orders);
        return "admin/orders/list";
    }

    // Cập nhật trạng thái đơn hàng (Duyệt, Không duyệt, Bàn giao vận chuyển)
    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable("id") Long orderId,
                                    @RequestParam("status") OrderStatus status,
                                    RedirectAttributes redirect) {
        try {
            orderService.updateOrderStatus(orderId, status);
            redirect.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng #" + orderId + " thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}

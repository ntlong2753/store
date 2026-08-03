package com.codegym.store.controller;

import com.codegym.store.model.Cart;
import com.codegym.store.model.Order;
import com.codegym.store.model.OrderStatus;
import com.codegym.store.model.User;
import com.codegym.store.model.UserAddress;
import com.codegym.store.repository.CartRepository;
import com.codegym.store.repository.UserAddressRepository;
import com.codegym.store.repository.UserRepository;
import com.codegym.store.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final UserAddressRepository userAddressRepository;

    public OrderController(OrderService orderService, UserRepository userRepository, CartRepository cartRepository, UserAddressRepository userAddressRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.userAddressRepository = userAddressRepository;
    }

    // Hiển thị trang thanh toán
    @GetMapping("/checkout")
    public String showCheckoutPage(Principal principal, Model model, RedirectAttributes redirect) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống!");
            return "redirect:/cart";
        }

        // Lấy địa chỉ mặc định để điền sẵn vào form
        UserAddress defaultAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(user.getId()).orElse(null);

        model.addAttribute("cart", cart);
        model.addAttribute("user", user);
        model.addAttribute("defaultAddress", defaultAddress);

        return "store/checkout";
    }

    // Xử lý tạo đơn hàng
    @PostMapping("/order/create")
    public String createOrder(
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("receiverEmail") String receiverEmail,
            @RequestParam("shippingAddress") String shippingAddress,
            Principal principal,
            RedirectAttributes redirect) {

        if (principal == null) return "redirect:/login";
        User user = userRepository.findByUsername(principal.getName()).orElse(null);

        try {
            Order order = orderService.createOrderFromCart(user, receiverName, receiverPhone, receiverEmail, shippingAddress);
            redirect.addFlashAttribute("successMessage", "Đặt hàng thành công! Mã đơn hàng của bạn là #" + order.getId());
            return "redirect:/orders";
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi đặt hàng: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    // Xem lịch sử đơn hàng của User
    @GetMapping("/orders")
    public String viewUserOrders(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        User user = userRepository.findByUsername(principal.getName()).orElse(null);

        if (user != null) {
            List<Order> orders = orderService.getUserOrders(user.getId());
            model.addAttribute("orders", orders);
        }

        return "store/orders";
    }

    // Nút xác nhận đã nhận hàng (do User bấm)
    @PostMapping("/order/{id}/confirm-received")
    public String confirmReceived(@PathVariable("id") Long orderId, Principal principal, RedirectAttributes redirect) {
        if (principal == null) return "redirect:/login";

        Order order = orderService.getOrderById(orderId);
        if (order != null && order.getStatus() == OrderStatus.SHIPPING) {
            orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);
            redirect.addFlashAttribute("successMessage", "Xác nhận nhận hàng thành công! Cảm ơn bạn đã mua sắm.");
        } else {
            redirect.addFlashAttribute("errorMessage", "Không thể xác nhận đơn hàng này.");
        }
        return "redirect:/orders";
    }
}

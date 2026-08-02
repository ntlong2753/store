package com.codegym.store.controller;

import com.codegym.store.model.Cart;
import com.codegym.store.model.User;
import com.codegym.store.repository.CartRepository;
import com.codegym.store.repository.UserRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final CartRepository cartRepository; // Bổ sung CartRepository

    public GlobalControllerAdvice(UserRepository userRepository, CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    // --- 1. Bơm Avatar cho Header ---
    @ModelAttribute("globalAvatar")
    public String addGlobalAvatarToModel(Principal principal) {
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null && user.getUserAvatar() != null) {
                return user.getUserAvatar().getPath();
            }
        }
        return null;
    }

    // --- 2. Bơm Số lượng Giỏ Hàng cho Header ---
    @ModelAttribute("cartCount")
    public int addCartCountToModel(Principal principal) {
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
                if (cart != null) {
                    // ĐÚNG YÊU CẦU: Chỉ trả về số LƯỢNG MẶT HÀNG (size của danh sách), KHÔNG cộng dồn quantity
                    return cart.getItems().size();
                }
            }
        }
        return 0; // Trả về 0 nếu chưa đăng nhập hoặc chưa có giỏ
    }
}

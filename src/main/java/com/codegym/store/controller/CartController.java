package com.codegym.store.controller;

import com.codegym.store.model.Cart;
import com.codegym.store.model.CartItem;
import com.codegym.store.model.Product;
import com.codegym.store.model.User;
import com.codegym.store.repository.CartRepository;
import com.codegym.store.repository.ProductRepository;
import com.codegym.store.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartController(CartRepository cartRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // 1. Giao diện trang Giỏ Hàng (Sẽ làm ở Bước 3)
    @GetMapping
    public String viewCart(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user != null) {
            // Lấy giỏ hàng của User ra, nếu chưa từng có giỏ thì tạo mới 1 cái rỗng
            Cart cart = cartRepository.findByUserId(user.getId()).orElse(new Cart());
            model.addAttribute("cart", cart);
        }
        return "store/cart";
    }

    // 2. Thêm một sản phẩm vào Giỏ Hàng
    @PostMapping("/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            Principal principal,
                            HttpServletRequest request,
                            RedirectAttributes redirect) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        Product product = productRepository.findById(productId).orElse(null); // Lấy sản phẩm dựa vào ID

        if (user != null && product != null) {
            Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
            }

            // Tìm xem sản phẩm này đã từng nằm trong giỏ chưa
            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (existingItem.isPresent()) {
                // Đã có trong giỏ thì chỉ cộng dồn số lượng
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
            } else {
                // Chưa có thì tạo một dòng mới trong giỏ
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                cart.getItems().add(newItem);
            }

            cartRepository.save(cart); // Lưu vào Database
            redirect.addFlashAttribute("successMessage", "Đã thêm " + product.getName() + " vào giỏ hàng!");
        }

        // Tuyệt chiêu: Trả người dùng về đúng cái trang mà họ vừa đứng bấm nút
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    // 2.5 Mua ngay (Thêm vào giỏ và chuyển sang trang giỏ hàng/thanh toán)
    @PostMapping("/buy-now")
    public String buyNow(@RequestParam("productId") Long productId,
                         @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                         Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        Product product = productRepository.findById(productId).orElse(null);

        if (user != null && product != null) {
            Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
            }

            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                cart.getItems().add(newItem);
            }

            cartRepository.save(cart);
        }

        return "redirect:/cart";
    }

    // 3. Xóa một mặt hàng khỏi giỏ
    @PostMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Long itemId, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userRepository.findByUsername(principal.getName()).orElse(null);

        if (user != null) {
            Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
            if (cart != null) {
                // Dùng orphanRemoval = true nên chỉ cần xóa khỏi List ở trên RAM là Hibernate tự bắn query xóa trong CSDL
                cart.getItems().removeIf(item -> item.getId().equals(itemId));
                cartRepository.save(cart);
            }
        }
        return "redirect:/cart";
    }

    // 4. Cập nhật số lượng
    @PostMapping("/update")
    public String updateQuantity(@RequestParam("itemId") Long itemId,
                                 @RequestParam("quantity") int quantity,
                                 Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user != null) {
            Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
            if (cart != null) {
                for (CartItem item : cart.getItems()) {
                    if (item.getId().equals(itemId)) {
                        // Chặn trường hợp người dùng cố tình nhập số âm
                        item.setQuantity(Math.max(1, quantity));
                        break;
                    }
                }
                cartRepository.save(cart);
            }
        }
        return "redirect:/cart";
    }

}

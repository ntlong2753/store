package com.codegym.store.controller;

import com.codegym.store.model.User;
import com.codegym.store.repository.UserAddressRepository;
import com.codegym.store.repository.UserRepository;
import com.codegym.store.service.StorageService;
import com.codegym.store.service.OrderService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final StorageService storageService;
    private final PasswordEncoder passwordEncoder;
    private final UserAddressRepository userAddressRepository;
    private final OrderService orderService;

    public ProfileController(UserRepository userRepository, StorageService storageService, PasswordEncoder passwordEncoder, UserAddressRepository userAddressRepository, OrderService orderService) {
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.passwordEncoder = passwordEncoder;
        this.userAddressRepository = userAddressRepository;
        this.orderService = orderService;
    }

    // 1. TRANG THÔNG TIN TÀI KHOẢN
    @GetMapping({"", "/"})
    public String showProfile(Principal principal, Model model) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "profile/info";
    }

    // 2. XỬ LÝ LƯU THÔNG TIN (Dùng @ModelAttribute hứng cả cục User)
    // 2. XỬ LÝ LƯU THÔNG TIN (Dùng @ModelAttribute hứng cả cục User)
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User userForm,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        User existingUser = userRepository.findByUsername(principal.getName()).orElse(null);
        if (existingUser != null) {
            existingUser.setFullName(userForm.getFullName());
            existingUser.setEmail(userForm.getEmail());
            existingUser.setPhone(userForm.getPhone());

            // Lưu ảnh vào bảng UserAvatar thay vì bảng User
            if (avatarFile != null && !avatarFile.isEmpty()) {
                String imagePath = storageService.storeFile(avatarFile);

                // Kiểm tra xem đã có dòng dữ liệu avatar nào chưa
                if (existingUser.getUserAvatar() == null) {
                    com.codegym.store.model.UserAvatar avatar = new com.codegym.store.model.UserAvatar();
                    avatar.setPath(imagePath);
                    existingUser.setUserAvatar(avatar); // Nhúng avatar mới vào User
                } else {
                    // Đã có rồi thì chỉ cập nhật lại đường dẫn
                    existingUser.getUserAvatar().setPath(imagePath);
                }
            }

            userRepository.save(existingUser);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        }

        return "redirect:/profile";
    }


    // 3. TRANG LỊCH SỬ ĐƠN HÀNG
    @GetMapping("/orders")
    public String showOrders(Principal principal, Model model) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("orders", orderService.getCompletedUserOrders(user.getId()));
        }
        model.addAttribute("user", user);
        return "profile/orders";
    }

    // 4. TRANG ĐỔI MẬT KHẨU
    @GetMapping("/password")
    public String showPasswordPage(Principal principal, Model model) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        model.addAttribute("user", user);
        return "profile/password";
    }

    // --- Tạo DTO nội bộ để gói gọn dữ liệu form Đổi mật khẩu ---
    @lombok.Data
    public static class PasswordDto {
        private String oldPassword;
        private String newPassword;
        private String confirmPassword;
    }

    // 5. LƯU MẬT KHẨU MỚI (Dùng @ModelAttribute hứng 1 Object duy nhất)
    @PostMapping("/password")
    public String updatePassword(@ModelAttribute PasswordDto passwordDto,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(passwordDto.getOldPassword(), user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cũ không chính xác!");
            return "redirect:/profile/password";
        }

        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "redirect:/profile/password";
        }

        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        return "redirect:/profile/password";
    }

    // ================== KHU VỰC SỔ ĐỊA CHỈ ==================

    // 1. Giao diện Danh sách địa chỉ
    @GetMapping("/addresses")
    public String showAddresses(Principal principal, Model model) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("addresses", userAddressRepository.findByUserIdOrderByIdDesc(user.getId()));
        }
        return "profile/addresses";
    }

    // 2. Thêm mới 1 địa chỉ
    @PostMapping("/addresses/add")
    public String addAddress(@RequestParam("receiverName") String receiverName,
                             @RequestParam("phone") String phone,
                             @RequestParam("fullAddress") String fullAddress,
                             @RequestParam(value = "isDefault", required = false) boolean isDefault,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {

        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        // Xử lý thông minh: Nếu địa chỉ mới được tích làm "Mặc định", phải gỡ "Mặc định" của các địa chỉ cũ đi
        if (isDefault) {
            java.util.List<com.codegym.store.model.UserAddress> oldAddresses = userAddressRepository.findByUserId(user.getId());
            for (com.codegym.store.model.UserAddress old : oldAddresses) {
                if (old.isDefault()) {
                    old.setDefault(false);
                    userAddressRepository.save(old);
                }
            }
        } else {
            // Còn nếu người dùng chưa có địa chỉ nào bao giờ, thì cái đầu tiên thêm vào tự động làm mặc định luôn
            if (userAddressRepository.findByUserId(user.getId()).isEmpty()) {
                isDefault = true;
            }
        }

        com.codegym.store.model.UserAddress newAddress = new com.codegym.store.model.UserAddress();
        newAddress.setReceiverName(receiverName);
        newAddress.setPhone(phone);
        newAddress.setFullAddress(fullAddress);
        newAddress.setDefault(isDefault);
        newAddress.setUser(user);

        userAddressRepository.save(newAddress);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm địa chỉ mới thành công!");

        return "redirect:/profile/addresses";
    }

    // 3. Nút bấm Đặt Làm Mặc Định
    @PostMapping("/addresses/default/{id}")
    public String setDefaultAddress(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        com.codegym.store.model.UserAddress targetAddress = userAddressRepository.findById(id).orElse(null);

        // Kiểm tra đúng địa chỉ của ông này mới cho sửa (Chống hack truyền ID láo trên URL)
        if (targetAddress != null && targetAddress.getUser().getId().equals(user.getId())) {
            // Gỡ mặc định các cái cũ
            java.util.List<com.codegym.store.model.UserAddress> allAddresses = userAddressRepository.findByUserId(user.getId());
            for (com.codegym.store.model.UserAddress addr : allAddresses) {
                if (addr.isDefault()) {
                    addr.setDefault(false);
                    userAddressRepository.save(addr);
                }
            }
            // Kích hoạt mặc định cho cái mới
            targetAddress.setDefault(true);
            userAddressRepository.save(targetAddress);
        }

        return "redirect:/profile/addresses";
    }

    // 4. Xóa Địa Chỉ
    @PostMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        com.codegym.store.model.UserAddress targetAddress = userAddressRepository.findById(id).orElse(null);
        if (targetAddress != null && targetAddress.getUser().getId().equals(user.getId())) {

            boolean wasDefault = targetAddress.isDefault();
            userAddressRepository.delete(targetAddress);

            // Xóa thông minh: Nếu vô tình xóa luôn cái địa chỉ Mặc Định, hệ thống sẽ lấy 1 cái khác đắp lên làm mặc định bù vào
            if (wasDefault) {
                java.util.List<com.codegym.store.model.UserAddress> remain = userAddressRepository.findByUserIdOrderByIdDesc(user.getId());
                if (!remain.isEmpty()) {
                    com.codegym.store.model.UserAddress newDefault = remain.get(0);
                    newDefault.setDefault(true);
                    userAddressRepository.save(newDefault);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa địa chỉ thành công.");
        }
        return "redirect:/profile/addresses";
    }

}

package com.codegym.store.controller;

import com.codegym.store.model.Cpu;
import com.codegym.store.model.Product;
import com.codegym.store.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final ProductRepository productRepository;
    private final CpuRepository cpuRepository;
    private final RamRepository ramRepository;
    private final VgaRepository vgaRepository;
    private final StorageRepository storageRepository;
    // Thêm Mainboard, Case, Psu... tùy theo bạn đã tạo
    public HomeController(ProductRepository productRepository, CpuRepository cpuRepository,
                          RamRepository ramRepository, VgaRepository vgaRepository,
                          StorageRepository storageRepository) {
        this.productRepository = productRepository;
        this.cpuRepository = cpuRepository;
        this.ramRepository = ramRepository;
        this.vgaRepository = vgaRepository;
        this.storageRepository = storageRepository;
    }

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "false") boolean viewAll,
                       @RequestParam(required = false) String cat, // Biến này để bắt lệnh lọc danh mục
                       Model model, Principal principal, HttpServletRequest request) {
        List<? extends Product> products = new java.util.ArrayList<>();
        String catTitle = "Tất Cả Sản Phẩm";
        // Tùy theo danh mục được ấn mà lôi dữ liệu tương ứng ra
        if (cat == null || cat.isEmpty()) {
            products = productRepository.findAll();
            catTitle = "Sản Phẩm Nổi Bật";
        } else if ("cpu".equals(cat)) {
            products = cpuRepository.findAll();
            catTitle = "Vi Xử Lý (CPU)";
        } else if ("ram".equals(cat)) {
            products = ramRepository.findAll();
            catTitle = "Bộ Nhớ Trong (RAM)";
        } else if ("vga".equals(cat)) {
            products = vgaRepository.findAll();
            catTitle = "Card Màn Hình (VGA)";
        } else if ("storage".equals(cat)) {
            products = storageRepository.findAll();
            catTitle = "Ổ Cứng (SSD/HDD)";
        }
        // Thêm else if ("mainboard".equals(cat))...
        model.addAttribute("cpus", products);
        model.addAttribute("viewAll", viewAll);
        model.addAttribute("catTitle", catTitle); // Truyền tiêu đề ra màn hình
        // Các đoạn set isAdmin cũ giữ nguyên...
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("isAdmin", request.isUserInRole("ROLE_ADMIN"));
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
    public String productDetail(@PathVariable Long id, Model model, Principal principal, HttpServletRequest request) {

        // ĐỔI SANG PRODUCT REPOSITORY ĐỂ TÌM ĐƯỢC MỌI LINH KIỆN
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/";
        }

        // Vẫn cứ lưu tên biến là "cpu" đẩy xuống HTML để đỡ phải sửa lắt nhắt
        model.addAttribute("cpu", product);

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
    @ResponseBody
    public List<Map<String, Object>> searchApi(@RequestParam String keyword) {

        // THAY ĐỔI 1: Dùng productRepository để tìm kiếm TRÊN TOÀN BỘ SẢN PHẨM thay vì chỉ cpuRepository
        List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);

        List<Map<String, Object>> results = new ArrayList<>();

        // THAY ĐỔI 2: Đổi tên biến cpus thành products cho logic
        for (Product product : products) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", product.getId());
            map.put("name", product.getName());

            DecimalFormat df = new DecimalFormat("#,###");
            map.put("price", df.format(product.getPrice()).replace(",", ".") + " ₫");

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                map.put("image", product.getImages().get(0).getPath());
            } else {
                map.put("image", "https://via.placeholder.com/50x50?text=No+Image");
            }
            results.add(map);
        }
        return results;
    }



}

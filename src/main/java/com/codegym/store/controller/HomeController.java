package com.codegym.store.controller;

import com.codegym.store.model.Product;
import com.codegym.store.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final MainboardRepository mainboardRepository;
    private final CasepcRepository casepcRepository;
    private final PsuRepository psuRepository;

    public HomeController(ProductRepository productRepository, CpuRepository cpuRepository,
                          RamRepository ramRepository, VgaRepository vgaRepository,
                          StorageRepository storageRepository,
                          MainboardRepository mainboardRepository,
                          CasepcRepository casepcRepository,
                          PsuRepository psuRepository) { 
        this.productRepository = productRepository;
        this.cpuRepository = cpuRepository;
        this.ramRepository = ramRepository;
        this.vgaRepository = vgaRepository;
        this.storageRepository = storageRepository;
        this.mainboardRepository = mainboardRepository; 
        this.casepcRepository = casepcRepository;
        this.psuRepository = psuRepository;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String cat, // Lọc theo danh mục
                       @RequestParam(defaultValue = "0") int page, // Bắt số trang hiện tại
                       Model model, Principal principal, HttpServletRequest request) {

        Page<? extends Product> productPage = null;
        String catTitle = "Tất Cả Sản Phẩm";

        // Cài đặt phân trang: lấy trang số 'page', mỗi trang 10 sản phẩm
        Pageable pageable = PageRequest.of(page, 50);

        // Thay vì findAll() thông thường, ta gọi findAll(pageable)
        if (cat == null || cat.isEmpty()) {
            productPage = productRepository.findAll(pageable);
            catTitle = "Sản Phẩm Nổi Bật";
        } else if ("cpu".equals(cat)) {
            productPage = cpuRepository.findAll(pageable);
            catTitle = "Vi Xử Lý (CPU)";
        } else if ("ram".equals(cat)) {
            productPage = ramRepository.findAll(pageable);
            catTitle = "Bộ Nhớ Trong (RAM)";
        } else if ("vga".equals(cat)) {
            productPage = vgaRepository.findAll(pageable);
            catTitle = "Card Màn Hình (VGA)";
        } else if ("storage".equals(cat)) {
            productPage = storageRepository.findAll(pageable);
            catTitle = "Ổ Cứng (SSD/HDD)";
        } else if ("mainboard".equals(cat)) { 
            productPage = mainboardRepository.findAll(pageable);
            catTitle = "Bo Mạch Chủ (Mainboard)";
        } else if ("casepc".equals(cat)) { 
            productPage = casepcRepository.findAll(pageable);
            catTitle = "Vỏ Máy Tính (Case)";
        } else if ("psu".equals(cat)) { 
            productPage = psuRepository.findAll(pageable);
            catTitle = "Nguồn Máy Tính (PSU)";
        }


        // .getContent() để chuyển Page thành List và hiển thị ra HTML
        model.addAttribute("cpus", productPage.getContent());

        // Truyền tổng số trang và trang hiện tại ra giao diện để vẽ nút bấm
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        model.addAttribute("viewAll", true); // Giữ biến này cho code cũ khỏi lỗi
        model.addAttribute("catTitle", catTitle);

        if (principal != null) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("isAdmin", request.isUserInRole("ROLE_ADMIN"));
        } else {
            model.addAttribute("isAdmin", false);
        }
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("isAdmin", request.isUserInRole("ROLE_ADMIN"));
        } else {
            model.addAttribute("isAdmin", false);
        }
        // --- COPY 4 DÒNG NÀY DÁN VÀO TRƯỚC LỆNH RETURN CUỐI CÙNG ---
        // Nếu trình duyệt gửi yêu cầu bằng AJAX, CHỈ trả về mẩu HTML danh sách sản phẩm
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return "store/product-list";
        }

        // Trả về toàn bộ trang chủ nếu là tải trang web bình thường
        return "store/home";
    }


    @GetMapping("/search")
    public String search(@RequestParam String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         Model model,
                         Principal principal,
                         jakarta.servlet.http.HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page, 50);
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);

        model.addAttribute("cpus", productPage.getContent());
        model.addAttribute("catTitle", "Kết quả tìm kiếm cho: " + keyword);
        model.addAttribute("viewAll", true);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        if (principal != null) {
            model.addAttribute("isAdmin", request.isUserInRole("ROLE_ADMIN"));
        } else {
            model.addAttribute("isAdmin", false);
        }

        return "store/home";
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

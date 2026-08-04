package com.codegym.store.controller;

import com.codegym.store.model.Cpu;
import com.codegym.store.model.ProductImage;
import com.codegym.store.repository.CpuRepository;
import com.codegym.store.service.ProductService;
import com.codegym.store.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/cpu")
public class CpuController {
    private final ProductService productService;
    private final CpuRepository cpuRepository;
    private final StorageService storageService;

    public CpuController(ProductService productService, CpuRepository cpuRepository, StorageService storageService) {
        this.productService = productService;
        this.cpuRepository = cpuRepository;
        this.storageService = storageService;
    }

    @GetMapping({"", "/"}) // Truy cập /cpu hoặc /cpu/ thì gọi hàm này
    public String showCpuList(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 10, org.springframework.data.domain.Sort.by("id").descending());
        Page<Cpu> cpuPage = cpuRepository.findAll(pageable);
        model.addAttribute("cpus", cpuPage);
        return "cpu/list-cpu";
    }

    // mở trang hiển thị Form thêm CPU
    @GetMapping("/add")
    public String showAddCpuForm(Model model) {
        model.addAttribute("product", new Cpu());
        return "cpu/add-cpu";
    }

    // nhận dữ liệu từ Form gửi về
    @PostMapping("/add")
    public String saveCpu(@ModelAttribute("product") Cpu cpu, BindingResult bindingResult,
                          @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        if (bindingResult.hasErrors()) {
            return "cpu/add-cpu";
        }

        if (cpu.getCores() > cpu.getThreads()) {
            bindingResult.rejectValue("cores", "error.cpu", "Số nhân không được lớn hơn số luồng");
            return "cpu/add-cpu";
        }

        // === ĐOẠN CODE MỚI: XỬ LÝ LƯU ẢNH ===
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    // Nhờ service lưu file vào ổ cứng
                    String imagePath = storageService.storeFile(file);

                    // Tạo đối tượng ProductImage và liên kết với CPU này
                    ProductImage productImage = new ProductImage();
                    productImage.setPath(imagePath);
                    productImage.setProduct(cpu);

                    // Nhét ảnh vào danh sách ảnh của CPU
                    cpu.getImages().add(productImage);
                }
            }
        }
        // ===================================

        generateName(cpu);
        // --- KẾT THÚC ---
        productService.save(cpu);
        return "redirect:/admin/cpu";
    }

    @GetMapping("/delete/{id}")
    public String deleteCpu(@PathVariable("id") Long id) {
        // Nhờ Service (đã được viết sẵn hàm remove) để xóa theo ID
        productService.remove(id);

        // Xóa xong thì quay lại trang danh sách để thấy nó biến mất
        return "redirect:/admin/cpu";
    }

    // 1. Nhận ID từ URL, móc dữ liệu cũ lên và mở Form Edit
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        // Tìm CPU trong database theo ID
        // Nếu không tìm thấy thì báo lỗi, nếu thấy thì lấy ra
        Cpu cpu = (Cpu) productService.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy CPU này"));

        // Đẩy dữ liệu cũ sang Form để nó tự điền vào các ô input
        model.addAttribute("product", cpu);
        return "cpu/edit-cpu";
    }

    // 2. Nhận dữ liệu sau khi người dùng bấm Lưu ở Form Edit
    @PostMapping("/edit")
    public String updateCpu(@Valid @ModelAttribute("product") Cpu cpu, BindingResult bindingResult,
                            @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
                            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {

        if (bindingResult.hasErrors()) {
            return "cpu/edit-cpu";
        }

        if (cpu.getCores() > cpu.getThreads()) {
            bindingResult.rejectValue("cores", "error.cpu", "Số nhân không được lớn hơn số luồng");
            return "cpu/edit-cpu";
        }

        // 1. Load entity từ DB
        Cpu existingCpu = (Cpu) productService.findById(cpu.getId()).orElse(null);

        if (existingCpu != null) {
            // 2. Cập nhật các trường text
            existingCpu.setSeries(cpu.getSeries());
            existingCpu.setSegment(cpu.getSegment());
            existingCpu.setModelNumber(cpu.getModelNumber());
            existingCpu.setSuffix(cpu.getSuffix());
            existingCpu.setCores(cpu.getCores());
            existingCpu.setThreads(cpu.getThreads());
            existingCpu.setSocket(cpu.getSocket());
            existingCpu.setDescription(cpu.getDescription());
            existingCpu.setStock(cpu.getStock());
            existingCpu.setPrice(cpu.getPrice());

            // 3. Xử lý xóa ảnh cũ (orphanRemoval sẽ tự xóa trong DB khi remove khỏi list)
            if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
                for (ProductImage img : existingCpu.getImages()) {
                    if (deletedImageIds.contains(img.getId())) {
                        storageService.deleteFile(img.getPath());
                    }
                }
                existingCpu.getImages().removeIf(img -> deletedImageIds.contains(img.getId()));
            }

            // 4. Thêm ảnh mới vào collection
            if (imageFiles != null) {
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        String imagePath = storageService.storeFile(file);
                        ProductImage newImg = new ProductImage();
                        newImg.setPath(imagePath);
                        newImg.setProduct(existingCpu);
                        existingCpu.getImages().add(newImg);
                    }
                }
            }

            // Sinh lại tên
            generateName(existingCpu);
            productService.save(existingCpu);
        }

        return "redirect:/admin/cpu";
    }



    private static void generateName(Cpu cpu) {
        // --- BẮT ĐẦU: Logic tự sinh tên CPU ---
        // Ghép chuỗi theo công thức: Dòng + Phân khúc + Mã + Hậu tố (Core i9 14900 K)
        // Dùng đoạn check hậu tố để tránh chữ "null" hiện lên nếu sản phẩm không có hậu tố (ví dụ: Core i5 12400)
        String suffix = (cpu.getSuffix() != null && !cpu.getSuffix().isEmpty()) ? cpu.getSuffix() : "";

        String autoName = cpu.getSeries() + " " + cpu.getSegment() + " " + cpu.getModelNumber() + suffix;

        // Cập nhật lại biến name cho Object CPU trước khi lưu
        cpu.setName(autoName.trim());
        // --- KẾT THÚC ---
    }

}

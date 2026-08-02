package com.codegym.store.controller;

import com.codegym.store.model.Cpu;
import com.codegym.store.repository.CpuRepository;
import com.codegym.store.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/cpu")
public class CpuController {
    private final ProductService productService;
    private final CpuRepository cpuRepository;

    public CpuController(ProductService productService, CpuRepository cpuRepository) {
        this.productService = productService;
        this.cpuRepository = cpuRepository;
    }

    @GetMapping({"", "/"}) // Truy cập /cpu hoặc /cpu/ thì gọi hàm này
    public String showCpuList(Model model) {
        // Lấy danh sách chuyên CPU
        model.addAttribute("cpus", cpuRepository.findAll());
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
    public String saveCpu(@ModelAttribute("product") Cpu cpu, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "cpu/add-cpu";
        }

        if (cpu.getCores() > cpu.getThreads()) {
            bindingResult.rejectValue("cores", "error.cpu", "Số nhân không được lớn hơn số luồng");
            return "cpu/add-cpu";
        }

        // --- BẮT ĐẦU: Logic tự sinh tên CPU ---
        // Ghép chuỗi theo công thức: Dòng + Phân khúc + Mã + Hậu tố (Core i9 14900 K)
        // Dùng đoạn check hậu tố để tránh chữ "null" hiện lên nếu sản phẩm không có hậu tố (ví dụ: Core i5 12400)
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
    public String updateCpu(@Valid @ModelAttribute("product") Cpu cpu, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "cpu/edit-cpu";
        }

        if (cpu.getCores() > cpu.getThreads()) {
            bindingResult.rejectValue("cores", "error.cpu", "Số nhân không được lớn hơn số luồng");
            return "cpu/edit-cpu";
        }

        generateName(cpu);

        productService.save(cpu);

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

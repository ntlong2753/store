package com.codegym.store.controller;

import com.codegym.store.model.Psu;
import com.codegym.store.model.ProductImage;
import com.codegym.store.repository.PsuRepository;
import com.codegym.store.service.ProductService;
import com.codegym.store.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Controller
@RequestMapping("/admin/psu")
public class PsuController {
    private final ProductService productService;
    private final PsuRepository psuRepository;
    private final StorageService fileStorageService;

    public PsuController(ProductService productService, PsuRepository psuRepository, StorageService fileStorageService) {
        this.productService = productService;
        this.psuRepository = psuRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping({"", "/"})
    public String showPsuList(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10, org.springframework.data.domain.Sort.by("id").descending());
        Page<Psu> psuPage = psuRepository.findAll(pageable);
        model.addAttribute("psus", psuPage);
        return "psu/list-psu";
    }

    @GetMapping("/add")
    public String showAddPsuForm(Model model) {
        model.addAttribute("product", new Psu());
        return "psu/add-psu";
    }

    @PostMapping("/add")
    public String savePsu(@Valid @ModelAttribute("product") Psu psu, BindingResult bindingResult,
                          @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        if (bindingResult.hasErrors()) {
            return "psu/add-psu";
        }

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imagePath = fileStorageService.storeFile(file);
                    ProductImage productImage = new ProductImage();
                    productImage.setPath(imagePath);
                    productImage.setProduct(psu);
                    psu.getImages().add(productImage);
                }
            }
        }

        // Tự sinh tên nếu để trống hoặc bạn có thể bắt buộc nhập. Ở form mẫu, Tên PSU "Tự sinh, có thể sửa".
        // Mình sẽ kết hợp Hãng và Công suất nếu tên chưa có.
        if (psu.getName() == null || psu.getName().trim().isEmpty()) {
            String brand = psu.getBrand() != null ? psu.getBrand() : "";
            String wattage = psu.getWattage() != null ? psu.getWattage() + "W" : "";
            psu.setName("Nguồn " + brand + " " + wattage);
        }

        productService.save(psu);
        return "redirect:/admin/psu";
    }

    @GetMapping("/delete/{id}")
    public String deletePsu(@PathVariable("id") Long id) {
        productService.remove(id);
        return "redirect:/admin/psu";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Psu psu = (Psu) productService.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Nguồn này"));
        model.addAttribute("product", psu);
        return "psu/edit-psu";
    }

    @PostMapping("/edit")
    public String updatePsu(@Valid @ModelAttribute("product") Psu psu, BindingResult bindingResult,
                            @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
                            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {

        if (bindingResult.hasErrors()) {
            return "psu/edit-psu";
        }

        // 1. Load entity từ DB
        Psu existingPsu = (Psu) productService.findById(psu.getId()).orElse(null);

        if (existingPsu != null) {
            existingPsu.setName(psu.getName());
            existingPsu.setBrand(psu.getBrand());
            existingPsu.setWattage(psu.getWattage());
            existingPsu.setFormFactor(psu.getFormFactor());
            existingPsu.setSize(psu.getSize());
            existingPsu.setDescription(psu.getDescription());
            existingPsu.setStock(psu.getStock());
            existingPsu.setPrice(psu.getPrice());

            // Xử lý xóa ảnh cũ
            if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
                for (ProductImage img : existingPsu.getImages()) {
                    if (deletedImageIds.contains(img.getId())) {
                        fileStorageService.deleteFile(img.getPath());
                    }
                }
                existingPsu.getImages().removeIf(img -> deletedImageIds.contains(img.getId()));
            }

            if (imageFiles != null) {
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        String imagePath = fileStorageService.storeFile(file);
                        ProductImage newImg = new ProductImage();
                        newImg.setPath(imagePath);
                        newImg.setProduct(existingPsu);
                        existingPsu.getImages().add(newImg);
                    }
                }
            }

            productService.save(existingPsu);
        }

        return "redirect:/admin/psu";
    }
}

package com.codegym.store.controller;

import com.codegym.store.model.Mainboard;
import com.codegym.store.model.ProductImage;
import com.codegym.store.repository.MainboardRepository;
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
@RequestMapping("/admin/mainboard")
public class MainboardController {
    private final ProductService productService;
    private final MainboardRepository mainboardRepository;
    private final StorageService storageService;

    public MainboardController(ProductService productService, MainboardRepository mainboardRepository, StorageService storageService) {
        this.productService = productService;
        this.mainboardRepository = mainboardRepository;
        this.storageService = storageService;
    }

    @GetMapping({"", "/"}) // Truy cập /mainboard hoặc /mainboard/ thì gọi hàm này
    public String showMainboardList(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 10, org.springframework.data.domain.Sort.by("id").descending());
        Page<Mainboard> mainboardPage = mainboardRepository.findAll(pageable);
        model.addAttribute("mainboards", mainboardPage);
        return "mainboard/list-mainboard";
    }

    @GetMapping("/add")
    public String showAddMainboardForm(Model model) {
        model.addAttribute("product", new Mainboard());
        return "mainboard/add-mainboard";
    }

    @PostMapping("/add")
    public String saveMainboard(@ModelAttribute("product") Mainboard mainboard, BindingResult bindingResult,
                          @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        if (bindingResult.hasErrors()) {
            return "mainboard/add-mainboard";
        }

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imagePath = storageService.storeFile(file);
                    ProductImage productImage = new ProductImage();
                    productImage.setPath(imagePath);
                    productImage.setProduct(mainboard);
                    mainboard.getImages().add(productImage);
                }
            }
        }

        generateName(mainboard);
        productService.save(mainboard);
        return "redirect:/admin/mainboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteMainboard(@PathVariable("id") Long id) {
        productService.remove(id);
        return "redirect:/admin/mainboard";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Mainboard mainboard = (Mainboard) productService.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Mainboard này"));
        model.addAttribute("product", mainboard);
        return "mainboard/edit-mainboard";
    }

    @PostMapping("/edit")
    public String updateMainboard(@Valid @ModelAttribute("product") Mainboard mainboard, BindingResult bindingResult,
                            @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
                            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {

        if (bindingResult.hasErrors()) {
            return "mainboard/edit-mainboard";
        }

        // 1. Load entity từ DB
        Mainboard existingMainboard = (Mainboard) productService.findById(mainboard.getId()).orElse(null);

        if (existingMainboard != null) {
            existingMainboard.setBrand(mainboard.getBrand());
            existingMainboard.setModel(mainboard.getModel());
            existingMainboard.setSocket(mainboard.getSocket());
            existingMainboard.setChipset(mainboard.getChipset());
            existingMainboard.setRamStandard(mainboard.getRamStandard());
            existingMainboard.setSize(mainboard.getSize());
            existingMainboard.setDescription(mainboard.getDescription());
            existingMainboard.setStock(mainboard.getStock());
            existingMainboard.setPrice(mainboard.getPrice());

            // Xử lý xóa ảnh cũ
            if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
                for (ProductImage img : existingMainboard.getImages()) {
                    if (deletedImageIds.contains(img.getId())) {
                        storageService.deleteFile(img.getPath());
                    }
                }
                existingMainboard.getImages().removeIf(img -> deletedImageIds.contains(img.getId()));
            }

            if (imageFiles != null) {
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        String imagePath = storageService.storeFile(file);
                        ProductImage newImg = new ProductImage();
                        newImg.setPath(imagePath);
                        newImg.setProduct(existingMainboard);
                        existingMainboard.getImages().add(newImg);
                    }
                }
            }

            generateName(existingMainboard);
            productService.save(existingMainboard);
        }

        return "redirect:/admin/mainboard";
    }

    private static void generateName(Mainboard mainboard) {
        String brand = mainboard.getBrand() != null ? mainboard.getBrand() : "";
        String model = mainboard.getModel() != null ? mainboard.getModel() : "";
        
        String autoName = "Mainboard " + brand + " " + model;
        mainboard.setName(autoName.trim().replaceAll(" +", " "));
    }
}

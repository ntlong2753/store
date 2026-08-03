package com.codegym.store.controller;

import com.codegym.store.model.Ram;
import com.codegym.store.model.ProductImage;
import com.codegym.store.repository.RamRepository;
import com.codegym.store.service.ProductService;
import com.codegym.store.service.StorageService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/ram")
public class RamController {
    private final ProductService productService;
    private final RamRepository ramRepository;
    private final StorageService storageService;

    public RamController(ProductService productService, RamRepository ramRepository, StorageService storageService) {
        this.productService = productService;
        this.ramRepository = ramRepository;
        this.storageService = storageService;
    }

    @GetMapping({"", "/"}) // Truy cập /ram hoặc /ram/ thì gọi hàm này
    public String showRamList(@RequestParam(defaultValue = "0") int page, Model model) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 10);
        org.springframework.data.domain.Page<com.codegym.store.model.Ram> ramPage = ramRepository.findAll(pageable);
        model.addAttribute("rams", ramPage);
        return "ram/list-ram";
    }

    @GetMapping("/add")
    public String showAddRamForm(Model model) {
        model.addAttribute("product", new Ram());
        return "ram/add-ram";
    }

    @PostMapping("/add")
    public String saveRam(@ModelAttribute("product") Ram ram, BindingResult bindingResult,
                          @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        if (bindingResult.hasErrors()) {
            return "ram/add-ram";
        }

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imagePath = storageService.storeFile(file);
                    ProductImage productImage = new ProductImage();
                    productImage.setPath(imagePath);
                    productImage.setProduct(ram);
                    ram.getImages().add(productImage);
                }
            }
        }

        generateName(ram);
        productService.save(ram);
        return "redirect:/admin/ram";
    }

    @GetMapping("/delete/{id}")
    public String deleteRam(@PathVariable("id") Long id) {
        productService.remove(id);
        return "redirect:/admin/ram";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Ram ram = (Ram) productService.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy RAM này"));
        model.addAttribute("product", ram);
        return "ram/edit-ram";
    }

    @PostMapping("/edit")
    public String updateRam(@Valid @ModelAttribute("product") Ram ram, BindingResult bindingResult,
                            @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
                            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {

        if (bindingResult.hasErrors()) {
            return "ram/edit-ram";
        }

        Ram existingRam = (Ram) productService.findById(ram.getId()).orElse(null);

        if (existingRam != null) {
            existingRam.setBrand(ram.getBrand());
            existingRam.setCapacity(ram.getCapacity());
            existingRam.setRamStandard(ram.getRamStandard());
            existingRam.setDescription(ram.getDescription());
            existingRam.setStock(ram.getStock());
            existingRam.setPrice(ram.getPrice());

            if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
                for (ProductImage img : existingRam.getImages()) {
                    if (deletedImageIds.contains(img.getId())) {
                        storageService.deleteFile(img.getPath());
                    }
                }
                existingRam.getImages().removeIf(img -> deletedImageIds.contains(img.getId()));
            }

            if (imageFiles != null) {
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        String imagePath = storageService.storeFile(file);
                        ProductImage newImg = new ProductImage();
                        newImg.setPath(imagePath);
                        newImg.setProduct(existingRam);
                        existingRam.getImages().add(newImg);
                    }
                }
            }

            generateName(existingRam);
            productService.save(existingRam);
        }

        return "redirect:/admin/ram";
    }

    private static void generateName(Ram ram) {
        String brand = ram.getBrand() != null ? ram.getBrand() : "";
        String capacity = ram.getCapacity() != null ? ram.getCapacity() : "";
        String standard = ram.getRamStandard() != null ? ram.getRamStandard() : "";
        
        String autoName = "RAM " + brand + " " + capacity + " " + standard;
        ram.setName(autoName.trim().replaceAll(" +", " "));
    }
}

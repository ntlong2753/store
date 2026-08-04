package com.codegym.store.controller;

import com.codegym.store.model.Vga;
import com.codegym.store.model.ProductImage;
import com.codegym.store.repository.VgaRepository;
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
@RequestMapping("/admin/vga")
public class VgaController {
    private final ProductService productService;
    private final VgaRepository vgaRepository;
    private final StorageService storageService;

    public VgaController(ProductService productService, VgaRepository vgaRepository, StorageService storageService) {
        this.productService = productService;
        this.vgaRepository = vgaRepository;
        this.storageService = storageService;
    }

    @GetMapping({"", "/"}) // Truy cập /vga hoặc /vga/ thì gọi hàm này
    public String showVgaList(@RequestParam(defaultValue = "0") int page, Model model) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, 10, org.springframework.data.domain.Sort.by("id").descending());
        org.springframework.data.domain.Page<com.codegym.store.model.Vga> vgaPage = vgaRepository.findAll(pageable);
        model.addAttribute("vgas", vgaPage);
        return "vga/list-vga";
    }

    @GetMapping("/add")
    public String showAddVgaForm(Model model) {
        model.addAttribute("product", new Vga());
        return "vga/add-vga";
    }

    @PostMapping("/add")
    public String saveVga(@ModelAttribute("product") Vga vga, BindingResult bindingResult,
                          @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        if (bindingResult.hasErrors()) {
            return "vga/add-vga";
        }

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imagePath = storageService.storeFile(file);
                    ProductImage productImage = new ProductImage();
                    productImage.setPath(imagePath);
                    productImage.setProduct(vga);
                    vga.getImages().add(productImage);
                }
            }
        }

        generateName(vga);
        productService.save(vga);
        return "redirect:/admin/vga";
    }

    @GetMapping("/delete/{id}")
    public String deleteVga(@PathVariable("id") Long id) {
        productService.remove(id);
        return "redirect:/admin/vga";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Vga vga = (Vga) productService.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy VGA này"));
        model.addAttribute("product", vga);
        return "vga/edit-vga";
    }

    @PostMapping("/edit")
    public String updateVga(@Valid @ModelAttribute("product") Vga vga, BindingResult bindingResult,
                            @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
                            @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {

        if (bindingResult.hasErrors()) {
            return "vga/edit-vga";
        }

        // 1. Load entity từ DB
        Vga existingVga = (Vga) productService.findById(vga.getId()).orElse(null);

        if (existingVga != null) {
            existingVga.setGpuBrand(vga.getGpuBrand());
            existingVga.setGpuModel(vga.getGpuModel());
            existingVga.setCardBrand(vga.getCardBrand());
            existingVga.setVram(vga.getVram());
            existingVga.setMemoryType(vga.getMemoryType());
            existingVga.setDescription(vga.getDescription());
            existingVga.setStock(vga.getStock());
            existingVga.setPrice(vga.getPrice());

            // Xử lý xóa ảnh cũ
            if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
                for (ProductImage img : existingVga.getImages()) {
                    if (deletedImageIds.contains(img.getId())) {
                        storageService.deleteFile(img.getPath());
                    }
                }
                existingVga.getImages().removeIf(img -> deletedImageIds.contains(img.getId()));
            }

            if (imageFiles != null) {
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        String imagePath = storageService.storeFile(file);
                        ProductImage newImg = new ProductImage();
                        newImg.setPath(imagePath);
                        newImg.setProduct(existingVga);
                        existingVga.getImages().add(newImg);
                    }
                }
            }

            generateName(existingVga);
            productService.save(existingVga);
        }

        return "redirect:/admin/vga";
    }

    private static void generateName(Vga vga) {
        String cardBrand = vga.getCardBrand() != null ? vga.getCardBrand() : "";
        String gpuModel = vga.getGpuModel() != null ? vga.getGpuModel() : "";
        String vram = vga.getVram() != null ? vga.getVram() + "GB" : "";
        String memoryType = vga.getMemoryType() != null ? vga.getMemoryType() : "";
        
        String autoName = cardBrand + " " + gpuModel + " " + vram + " " + memoryType;
        vga.setName(autoName.trim().replaceAll(" +", " "));
    }
}

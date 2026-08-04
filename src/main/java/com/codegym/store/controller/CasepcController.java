package com.codegym.store.controller;

import com.codegym.store.model.Casepc;
import com.codegym.store.model.ProductImage;
import com.codegym.store.repository.CasepcRepository;
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
@RequestMapping("/admin/casepc")
public class CasepcController {
    private final ProductService productService;
    private final CasepcRepository casepcRepository;
    private final StorageService fileStorageService;

    public CasepcController(ProductService productService, CasepcRepository casepcRepository, StorageService fileStorageService) {
        this.productService = productService;
        this.casepcRepository = casepcRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping({"", "/"})
    public String showCasepcList(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10, org.springframework.data.domain.Sort.by("id").descending());
        Page<Casepc> casepcPage = casepcRepository.findAll(pageable);
        model.addAttribute("casepcs", casepcPage);
        return "casepc/list-casepc";
    }

    @GetMapping("/add")
    public String showAddCasepcForm(Model model) {
        model.addAttribute("product", new Casepc());
        return "casepc/add-casepc";
    }

    @PostMapping("/add")
    public String saveCasepc(@Valid @ModelAttribute("product") Casepc casepc, BindingResult bindingResult,
                             @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {
        if (bindingResult.hasErrors()) {
            return "casepc/add-casepc";
        }

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imagePath = fileStorageService.storeFile(file);
                    ProductImage productImage = new ProductImage();
                    productImage.setPath(imagePath);
                    productImage.setProduct(casepc);
                    casepc.getImages().add(productImage);
                }
            }
        }

        generateName(casepc);
        productService.save(casepc);
        return "redirect:/admin/casepc";
    }

    @GetMapping("/delete/{id}")
    public String deleteCasepc(@PathVariable("id") Long id) {
        productService.remove(id);
        return "redirect:/admin/casepc";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Casepc casepc = (Casepc) productService.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Case này"));
        model.addAttribute("product", casepc);
        return "casepc/edit-casepc";
    }

    @PostMapping("/edit")
    public String updateCasepc(@Valid @ModelAttribute("product") Casepc casepc, BindingResult bindingResult,
                               @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
                               @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles) {

        if (bindingResult.hasErrors()) {
            return "casepc/edit-casepc";
        }

        // 1. Load entity từ DB
        Casepc existingCasepc = (Casepc) productService.findById(casepc.getId()).orElse(null);

        if (existingCasepc != null) {
            existingCasepc.setBrand(casepc.getBrand());
            existingCasepc.setModelNumber(casepc.getModelNumber());
            existingCasepc.setFormFactor(casepc.getFormFactor());
            existingCasepc.setSupportedMainboard(casepc.getSupportedMainboard());

            if (casepc.getName() != null && !casepc.getName().isEmpty()) {
                existingCasepc.setName(casepc.getName());
            }

            existingCasepc.setDescription(casepc.getDescription());
            existingCasepc.setStock(casepc.getStock());
            existingCasepc.setPrice(casepc.getPrice());

            // Xử lý xóa ảnh cũ
            if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
                for (ProductImage img : existingCasepc.getImages()) {
                    if (deletedImageIds.contains(img.getId())) {
                        fileStorageService.deleteFile(img.getPath());
                    }
                }
                existingCasepc.getImages().removeIf(img -> deletedImageIds.contains(img.getId()));
            }

            if (imageFiles != null) {
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        String imagePath = fileStorageService.storeFile(file);
                        ProductImage newImg = new ProductImage();
                        newImg.setPath(imagePath);
                        newImg.setProduct(existingCasepc);
                        existingCasepc.getImages().add(newImg);
                    }
                }
            }

            generateName(existingCasepc);
            productService.save(existingCasepc);
        }

        return "redirect:/admin/casepc";
    }

    private void generateName(Casepc casepc) {
        // Tên Case tự động sinh theo cấu trúc [Hãng] [Model] [Kích thước]
        // Chỉ sinh khi name đang trống hoặc giống mẫu mặc định
        if (casepc.getName() == null || casepc.getName().trim().isEmpty() || casepc.getName().startsWith("Case ")) {
            String brand = casepc.getBrand() != null ? casepc.getBrand() : "";
            String model = casepc.getModelNumber() != null ? casepc.getModelNumber() : "";
            String form = casepc.getFormFactor() != null ? casepc.getFormFactor() : "";
            String autoName = "Case " + brand + " " + model + " " + form;
            casepc.setName(autoName.trim().replaceAll(" +", " "));
        }
    }
}

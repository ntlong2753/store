package com.codegym.store.controller;

import com.codegym.store.model.Storage;
import com.codegym.store.model.ProductImage;
import com.codegym.store.repository.StorageRepository;
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
@RequestMapping("/admin/storage")
public class StorageController {
    private final ProductService productService;
    private final StorageRepository storageRepository;
    private final com.codegym.store.service.StorageService fileStorageService;

    public StorageController(ProductService productService, StorageRepository storageRepository, com.codegym.store.service.StorageService fileStorageService) {
        this.productService = productService;
        this.storageRepository = storageRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping({"", "/"})
    public String showStorageList(Model model) {
        model.addAttribute("storages", storageRepository.findAll());
        return "storage/list-storage";
    }

    @GetMapping("/add")
    public String showAddStorageForm(Model model) {
        model.addAttribute("product", new Storage());
        return "storage/add-storage";
    }

    @PostMapping("/add")
    public String saveStorage(@ModelAttribute("product") Storage storage, BindingResult bindingResult,
                              @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                              @RequestParam(value = "capacityUnit", defaultValue = "GB") String capacityUnit) {
        if (bindingResult.hasErrors()) {
            return "storage/add-storage";
        }
        
        // Gộp dung lượng và đơn vị
        if (storage.getCapacity() != null && !storage.getCapacity().trim().isEmpty() && !storage.getCapacity().contains(capacityUnit)) {
            storage.setCapacity(storage.getCapacity().trim() + " " + capacityUnit);
        }

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imagePath = fileStorageService.storeFile(file);
                    ProductImage productImage = new ProductImage();
                    productImage.setPath(imagePath);
                    productImage.setProduct(storage);
                    storage.getImages().add(productImage);
                }
            }
        }

        generateName(storage);
        productService.save(storage);
        return "redirect:/admin/storage";
    }

    @GetMapping("/delete/{id}")
    public String deleteStorage(@PathVariable("id") Long id) {
        productService.remove(id);
        return "redirect:/admin/storage";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Storage storage = (Storage) productService.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Storage này"));
        
        // Xử lý tách dung lượng và đơn vị cho giao diện nếu cần, nhưng ta có thể để thymeleaf xử lý chuỗi
        model.addAttribute("product", storage);
        return "storage/edit-storage";
    }

    @PostMapping("/edit")
    public String updateStorage(@Valid @ModelAttribute("product") Storage storage, BindingResult bindingResult,
                                @RequestParam(value = "deletedImageIds", required = false) List<Long> deletedImageIds,
                                @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                                @RequestParam(value = "capacityUnit", defaultValue = "GB") String capacityUnit) {

        if (bindingResult.hasErrors()) {
            return "storage/edit-storage";
        }

        Storage existingStorage = (Storage) productService.findById(storage.getId()).orElse(null);

        if (existingStorage != null) {
            existingStorage.setStorageType(storage.getStorageType());
            existingStorage.setBrand(storage.getBrand());
            
            String newCapacity = storage.getCapacity() != null ? storage.getCapacity().trim() : "";
            if (!newCapacity.isEmpty() && !newCapacity.endsWith("GB") && !newCapacity.endsWith("TB")) {
                 newCapacity = newCapacity + " " + capacityUnit;
            }
            existingStorage.setCapacity(newCapacity);
            
            existingStorage.setConnectionStandard(storage.getConnectionStandard());
            existingStorage.setPcieStandard(storage.getPcieStandard());
            existingStorage.setReadSpeed(storage.getReadSpeed());
            existingStorage.setWriteSpeed(storage.getWriteSpeed());
            existingStorage.setRpm(storage.getRpm());
            existingStorage.setCache(storage.getCache());
            
            existingStorage.setDescription(storage.getDescription());
            existingStorage.setStock(storage.getStock());
            existingStorage.setPrice(storage.getPrice());

            if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
                for (ProductImage img : existingStorage.getImages()) {
                    if (deletedImageIds.contains(img.getId())) {
                        fileStorageService.deleteFile(img.getPath());
                    }
                }
                existingStorage.getImages().removeIf(img -> deletedImageIds.contains(img.getId()));
            }

            if (imageFiles != null) {
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        String imagePath = fileStorageService.storeFile(file);
                        ProductImage newImg = new ProductImage();
                        newImg.setPath(imagePath);
                        newImg.setProduct(existingStorage);
                        existingStorage.getImages().add(newImg);
                    }
                }
            }

            generateName(existingStorage);
            productService.save(existingStorage);
        }

        return "redirect:/admin/storage";
    }

    private static void generateName(Storage storage) {
        String type = storage.getStorageType() != null ? storage.getStorageType() : "";
        String brand = storage.getBrand() != null ? storage.getBrand() : "";
        String capacity = storage.getCapacity() != null ? storage.getCapacity() : "";
        
        String autoName = "Ổ cứng " + type + " " + brand + " " + capacity;
        storage.setName(autoName.trim().replaceAll(" +", " "));
    }
}

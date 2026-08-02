package com.codegym.store.service.impl;

import com.codegym.store.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageServiceImpl implements StorageService {
    private final Path rootLocation = Paths.get("uploads");

    public FileStorageServiceImpl() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục uploads", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("File rỗng.");
            }

            // Lấy thẳng tên gốc (VD: anh_cpu.jpg). Nếu đã tồn tại thì nó tự động Ghi Đè (REPLACE_EXISTING)
            String filename = file.getOriginalFilename();
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/images/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu file.", e);
        }

    }

    @Override
    public void deleteFile(String fileName) {
        try {
            // Cắt bỏ phần "/images/" để lấy tên file gốc
            String actualFileName = fileName.replace("/images/", "");
            Path fileToDelete = rootLocation.resolve(actualFileName).normalize().toAbsolutePath();
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi xóa file.", e);
        }
    }
}

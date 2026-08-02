package com.codegym.store.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    // Hàm lưu ảnh, trả về đường dẫn để lưu DB
    String storeFile(MultipartFile file);

    // Hàm xóa ảnh trong ổ cứng khi không cần nữa
    void deleteFile(String fileName);
}

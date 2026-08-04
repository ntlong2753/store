package com.codegym.store.repository;

import com.codegym.store.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // Xóa ảnh theo danh sách ID bằng JPQL trực tiếp (không phụ thuộc orphanRemoval)
    @Modifying
    @Query("DELETE FROM ProductImage pi WHERE pi.id IN :ids")
    void deleteAllByIdIn(@Param("ids") List<Long> ids);

    // Lấy danh sách ảnh thuộc về 1 product (để lấy path trước khi xóa file)
    List<ProductImage> findByProductId(Long productId);
}

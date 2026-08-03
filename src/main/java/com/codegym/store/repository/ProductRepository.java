package com.codegym.store.repository;

import com.codegym.store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String keyword);
    org.springframework.data.domain.Page<Product> findByNameContainingIgnoreCase(String keyword, org.springframework.data.domain.Pageable pageable);

}

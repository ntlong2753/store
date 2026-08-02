package com.codegym.store.service.impl;

import com.codegym.store.model.Cpu;
import com.codegym.store.model.Product;
import com.codegym.store.repository.ProductRepository;
import com.codegym.store.service.ProductService;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Iterable<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public void save(Product product) {
        // Ép kiểu để kiểm tra nếu nó là CPU
        if (product instanceof Cpu cpu) {
            if (cpu.getCores() <= 0 || cpu.getThreads() <= 0) {
                throw new IllegalArgumentException("Số nhân và luồng phải lớn hơn 0");
            }
            if (cpu.getCores() > cpu.getThreads()) {
                throw new IllegalArgumentException("Số nhân không được lớn hơn số luồng");
            }
        }

        // Nếu kiểm tra đúng thì mới lưu vào DB
        productRepository.save(product);
    }


    @Override
    public void remove(Long id) {
        productRepository.deleteById(id);
    }
}

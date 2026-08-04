package com.codegym.store.service.impl;

import com.codegym.store.model.Cpu;
import com.codegym.store.model.Product;
import com.codegym.store.repository.CartItemRepository;
import com.codegym.store.repository.OrderItemRepository;
import com.codegym.store.repository.ProductImageRepository;
import com.codegym.store.repository.ProductRepository;
import com.codegym.store.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductImageRepository productImageRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CartItemRepository cartItemRepository,
                              OrderItemRepository orderItemRepository,
                              ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.productImageRepository = productImageRepository;
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
    @Transactional
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
    @Transactional
    public void deleteImages(List<Long> imageIds) {
        // Xóa trực tiếp bằng JPQL — đáng tin cậy hơn orphanRemoval trên detached entity
        if (imageIds != null && !imageIds.isEmpty()) {
            productImageRepository.deleteAllByIdIn(imageIds);
        }
    }

    @Override
    @Transactional
    public void remove(Long id) {
        // Xóa CartItem tham chiếu đến product này trước (tránh lỗi foreign key)
        cartItemRepository.deleteByProductId(id);

        // Xóa OrderItem tham chiếu đến product này trước (tránh lỗi foreign key)
        orderItemRepository.deleteByProductId(id);

        // Sau đó mới xóa product (cascade sẽ tự xóa ProductImage)
        productRepository.deleteById(id);
    }
}

package com.codegym.store.service;

import com.codegym.store.model.Product;
import com.codegym.store.repository.ProductRepository;

import java.util.List;

public interface ProductService extends GeneralService<Product, Long> {

    void deleteImages(List<Long> imageIds);
}


package com.jacent.storefront.service.impl;

import com.jacent.storefront.pojo.Product;
import com.jacent.storefront.repository.ProductRepository;
import com.jacent.storefront.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;

    ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getProducts() {
        return productRepository.findAll();
    }
}

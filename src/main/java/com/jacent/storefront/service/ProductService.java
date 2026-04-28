package com.jacent.storefront.service;

import com.jacent.storefront.pojo.Product;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ProductService {
    List<Product> getProducts();
}

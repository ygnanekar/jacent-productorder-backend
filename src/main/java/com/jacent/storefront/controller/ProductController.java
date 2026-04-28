package com.jacent.storefront.controller;

import com.jacent.storefront.dto.response.ProductsResponse;
import com.jacent.storefront.entity.Commodity;
import com.jacent.storefront.entity.Division;
import com.jacent.storefront.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1")
@RestController
public class ProductController {

    ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/divisions")
    public ResponseEntity<List<Division>> getAllDivisions() {
        return ResponseEntity.ok(productService.getAllDivisions());
    }

    @GetMapping("/commodities")
    public ResponseEntity<List<Commodity>> getAllCommodities() {
        return ResponseEntity.ok(productService.getAllCommodities());
    }

    @GetMapping("/products")
    public ResponseEntity<ProductsResponse> getProducts(@RequestParam(defaultValue = "0") Integer pageNo,
                                                                     @RequestParam(required = false) Integer pageSize) {
        return ResponseEntity.ok(productService.getProducts(pageNo, pageSize));
    }
}

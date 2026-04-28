package com.jacent.storefront.service.impl;

import com.jacent.storefront.dto.response.ProductsResponse;
import com.jacent.storefront.entity.Commodity;
import com.jacent.storefront.entity.Configuration;
import com.jacent.storefront.entity.Division;
import com.jacent.storefront.entity.Product;
import com.jacent.storefront.repository.CommodityRepository;
import com.jacent.storefront.repository.DivisionRepository;
import com.jacent.storefront.repository.ProductRepository;
import com.jacent.storefront.service.ConfigurationService;
import com.jacent.storefront.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ConfigurationService configurationService;
    private final ProductRepository productRepository;
    private final DivisionRepository divisionRepository;
    private final CommodityRepository commodityRepository;

    ProductServiceImpl(ProductRepository productRepository, ConfigurationService configurationService, DivisionRepository divisionRepository, CommodityRepository commodityRepository) {
        this.productRepository = productRepository;
        this.configurationService = configurationService;
        this.divisionRepository = divisionRepository;
        this.commodityRepository = commodityRepository;
    }

    @Override
    public List<Division> getAllDivisions() {
        return divisionRepository.findAll();
    }

    @Override
    public List<Commodity> getAllCommodities() {
        return commodityRepository.findAll();
    }

    @Override
    public ProductsResponse getProducts(Integer pageNo, Integer pageSize) {
        if(pageSize == null){
            pageSize = configurationService.getValueAsInteger(Configuration.PAGINATION_SIZE, 25);
        }

        long total = productRepository.getTotalProductsCount();

        List<Product> productList = productRepository.getAllProductsPagination(pageNo, pageSize);

        return ProductsResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .content(productList)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / pageSize))
                .build();
    }
}

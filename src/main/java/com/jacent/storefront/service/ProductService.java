package com.jacent.storefront.service;

import com.jacent.storefront.dto.response.ProductsResponse;
import com.jacent.storefront.entity.Commodity;
import com.jacent.storefront.entity.Division;
import java.util.List;


public interface ProductService {
    List<Division> getAllDivisions();

    List<Commodity> getAllCommodities();

    ProductsResponse getProducts(Integer pageNo, Integer pageSize);
}

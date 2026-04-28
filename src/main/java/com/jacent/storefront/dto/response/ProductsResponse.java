package com.jacent.storefront.dto.response;

import com.jacent.storefront.entity.Product;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductsResponse {
    private List<Product> content;
    private int pageNo;
    private int pageSize;
    private Long totalElements;
    private int totalPages;
}

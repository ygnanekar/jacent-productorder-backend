package com.jacent.storefront.repository;

import com.jacent.storefront.query.ProductQueries;
import com.jacent.storefront.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ProductQueries productQueries;

    public List<Product> getAllProductsPagination(int page, int size) {
        int offset = page * size;

        return jdbcTemplate.query(
                productQueries.getAllProducts(),
                new Object[]{size, offset},
                new BeanPropertyRowMapper<>(Product.class)
        );
    }

    public int getTotalProductsCount() {
        return jdbcTemplate.queryForObject(productQueries.getProductCount(), Integer.class);
    }
}

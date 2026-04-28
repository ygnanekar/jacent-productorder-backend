package com.jacent.storefront.query;

import com.jacent.storefront.configuration.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@ConfigurationProperties(prefix = "product.queries")
@PropertySource(value = "classpath:queries/product-queries.yaml", factory = YamlPropertySourceFactory.class)
@Data
public class ProductQueries {

    private String productCount;
    private String allProducts;
}

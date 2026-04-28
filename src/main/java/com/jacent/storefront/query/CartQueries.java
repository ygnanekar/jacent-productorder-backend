package com.jacent.storefront.query;

import com.jacent.storefront.configuration.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "cart.queries")
@PropertySource(value = "classpath:queries/cart-queries.yaml", factory = YamlPropertySourceFactory.class)
@Data
public class CartQueries {
    private String cartByCartId;
    private String cartByUserId;
    private String createCart;
    private String addItemToCart;
    private String cartItemsByCartId;
    private String cartItemByCartIdAndProductId;
    private String cartItemByCartItemId;
    private String updateCartItemQuantity;
    private String deleteCart;
    private String deleteCartItemByCartItemId;
}

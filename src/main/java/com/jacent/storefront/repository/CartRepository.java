package com.jacent.storefront.repository;

import com.jacent.storefront.dto.request.CartItemRequest;
import com.jacent.storefront.entity.Cart;
import com.jacent.storefront.entity.CartItem;
import com.jacent.storefront.query.CartQueries;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CartRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    CartQueries cartQueries;

    public Optional<Cart> findCartByUserId(int userId) {
        try {
            Cart cart = jdbcTemplate.queryForObject(
                    cartQueries.getCartByUserId(),
                    new BeanPropertyRowMapper<>(Cart.class),
                    userId
            );
            return Optional.ofNullable(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Cart createCart(int userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    cartQueries.getCreateCart(),
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, userId);
            return ps;
        }, keyHolder);

        int generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        return findCartById(generatedId)
                .orElseThrow(() -> new RuntimeException("Failed to create cart for user: " + userId));
    }

    public Optional<Cart> findCartById(int cartId) {
        try {
            Cart cart = jdbcTemplate.queryForObject(
                    cartQueries.getCartByCartId(),
                    new BeanPropertyRowMapper<>(Cart.class),
                    cartId
            );
            return Optional.ofNullable(cart);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<CartItem> findItemsByCartId(int cartId) {
        return jdbcTemplate.query(
                cartQueries.getCartItemsByCartId(),
                new BeanPropertyRowMapper<>(CartItem.class),
                cartId
        );
    }

    public Optional<CartItem> findItemByCartIdAndProductId(int cartId, int productId) {
        try {
            CartItem item = jdbcTemplate.queryForObject(
                    cartQueries.getCartItemByCartIdAndProductId(),
                    new BeanPropertyRowMapper<>(CartItem.class),
                    cartId, productId
            );
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public CartItem addItemToCart(int cartId, CartItemRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    cartQueries.getAddItemToCart(),
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, cartId);
            ps.setInt(2, request.getProductId());
            ps.setInt(3, request.getQuantity());
            return ps;
        }, keyHolder);

        int generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        return findItemById(generatedId)
                .orElseThrow(() -> new RuntimeException("Failed to add cart item"));
    }

    public CartItem updateItemQuantity(int cartItemId, int quantity) {
        jdbcTemplate.update(
                cartQueries.getUpdateCartItemQuantity(),
                quantity, cartItemId
        );
        return findItemById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));
    }

    public void removeItem(int cartItemId) {
        jdbcTemplate.update(
                cartQueries.getDeleteCartItemByCartItemId(),
                cartItemId
        );
    }

    public void clearCart(int cartId) {
        jdbcTemplate.update(
                cartQueries.getDeleteCart(),
                cartId
        );
    }

    public Optional<CartItem> findItemById(int cartItemId) {
        try {
            CartItem item = jdbcTemplate.queryForObject(
                    cartQueries.getCartItemByCartItemId(),
                    new BeanPropertyRowMapper<>(CartItem.class),
                    cartItemId
            );
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

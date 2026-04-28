package com.jacent.storefront.service;

import com.jacent.storefront.dto.request.CartItemRequest;
import com.jacent.storefront.dto.response.CartResponse;
import jakarta.validation.Valid;

import java.nio.file.AccessDeniedException;

public interface CartService {
    CartResponse getCartByUser();

    CartResponse addItemToCart(@Valid CartItemRequest cartItemRequest);

    CartResponse updateItem(int cartItemId, @Valid CartItemRequest request) throws AccessDeniedException;

    CartResponse removeItem(int cartItemId) throws AccessDeniedException;

    void clearCart();


}

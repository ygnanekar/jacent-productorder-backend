package com.jacent.storefront.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CartItemResponse {
    private int cartItemId;
    private int productId;
    private int quantity;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
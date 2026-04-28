package com.jacent.storefront.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private int cartId;
    private int userId;
    private List<CartItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

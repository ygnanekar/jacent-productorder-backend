package com.jacent.storefront.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    private int cartId;
    private int userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

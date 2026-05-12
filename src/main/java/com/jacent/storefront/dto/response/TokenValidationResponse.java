package com.jacent.storefront.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenValidationResponse {
    private boolean valid;
    private String  tokenType;
}
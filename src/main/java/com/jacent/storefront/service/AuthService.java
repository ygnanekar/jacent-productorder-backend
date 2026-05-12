package com.jacent.storefront.service;

import com.jacent.storefront.dto.request.LoginRequest;
import com.jacent.storefront.dto.request.RefreshTokenRequest;
import com.jacent.storefront.dto.request.RegisterRequest;
import com.jacent.storefront.dto.response.AuthResponse;
import com.jacent.storefront.dto.response.TokenValidationResponse;
import com.jacent.storefront.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    UserResponse getMe();

    void initiateReset(String email);

    void completeReset(String token, String newPassword);

    void activateAccount(String token, String newPassword);

    TokenValidationResponse validateToken(String token);
}

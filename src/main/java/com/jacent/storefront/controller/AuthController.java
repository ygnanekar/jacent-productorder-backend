package com.jacent.storefront.controller;

import com.jacent.storefront.dto.request.LoginRequest;
import com.jacent.storefront.dto.request.RefreshTokenRequest;
import com.jacent.storefront.dto.request.RegisterRequest;
import com.jacent.storefront.dto.response.AuthResponse;
import com.jacent.storefront.dto.response.TokenValidationResponse;
import com.jacent.storefront.dto.response.UserResponse;
import com.jacent.storefront.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Stateless JWT: client simply discards the token
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.getMe());
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam(required =true) String token,  @RequestParam(required =true) String newPassword) {
        authService.activateAccount(token, newPassword);
        return ResponseEntity.ok("Account activated successfully. You can now log in.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam(required =true) String email) {
        authService.initiateReset(email);
        return ResponseEntity.ok("Reset link sent if the email exists.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam(required =true) String token,
            @RequestParam(required =true) String newPassword) {
        authService.completeReset(token, newPassword);
        return ResponseEntity.ok("Password updated successfully.");
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateVerificationToken(
            @RequestParam String token) {
        log.info("Verification token validation request — token: {}", token);
        try {
            TokenValidationResponse response = authService.validateToken(token);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Token is expired or invalid");
        }
    }
}

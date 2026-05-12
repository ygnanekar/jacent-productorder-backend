package com.jacent.storefront.service.impl;

import com.jacent.storefront.configuration.JwtConfig;
import com.jacent.storefront.dto.request.LoginRequest;
import com.jacent.storefront.dto.request.RefreshTokenRequest;
import com.jacent.storefront.dto.request.RegisterRequest;
import com.jacent.storefront.dto.response.AuthResponse;
import com.jacent.storefront.dto.response.TokenValidationResponse;
import com.jacent.storefront.dto.response.UserResponse;
import com.jacent.storefront.entity.TokenType;
import com.jacent.storefront.entity.VerificationToken;
import com.jacent.storefront.entity.User;
import com.jacent.storefront.exception.ResourceInvalidException;
import com.jacent.storefront.exception.ResourceNotFoundException;
import com.jacent.storefront.repository.UserRepository;
import com.jacent.storefront.repository.VerificationTokensRepository;
import com.jacent.storefront.security.JwtService;
import com.jacent.storefront.service.AuthService;
import com.jacent.storefront.service.EmailService;
import com.jacent.storefront.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final VerificationTokensRepository verificationTokensRepository;

    @Value("${app.token-expiry-hours:24}")
    private int tokenExpiryHours;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, JwtConfig jwtConfig, AuthenticationManager authenticationManager, EmailService emailService, VerificationTokensRepository verificationTokensRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtConfig = jwtConfig;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.verificationTokensRepository = verificationTokensRepository;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already in use: {}", request.getEmail());
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .storeId(request.getStoreId())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .build();
        userRepository.createUser(user);

        String username = user.getFirstName() + " " + user.getLastName();
        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = VerificationToken.builder()
                .token(token)
                .userId(user.getUserId())
                .tokenType(TokenType.ACCOUNT_ACTIVATION)
                .expiresAt(LocalDateTime.now().plusHours(tokenExpiryHours))
                .build();
        verificationTokensRepository.save(resetToken);

        emailService.sendWelcomeEmail(user.getEmail(), username, token);
        log.info("New user registered: email={}", request.getEmail());
        return toUserResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: email={}", request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail());
        log.info("Login successful: email={}", user.getEmail());
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String email;
        try {
            email = jwtService.extractRefreshTokenUsername(request.getRefreshToken());
        } catch (Exception e) {
            log.warn("Refresh token extraction failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid refresh token");
        }
        User user = userRepository.findByEmail(email);
        if (!jwtService.isRefreshTokenValid(request.getRefreshToken(), user)) {
            log.warn("Refresh token validation failed for: {}", email);
            throw new IllegalArgumentException("Refresh token is expired or invalid");
        }
        log.info("Access token refreshed for: {}", email);
        return buildAuthResponse(user);
    }

    @Override
    public UserResponse getMe() {
        return toUserResponse(SecurityUtils.getCurrentUser());
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        VerificationToken verificationToken = verificationTokensRepository.findByTokenAndType(token, null)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        return TokenValidationResponse.builder()
                .valid(verificationToken.isValid())
                .tokenType(verificationToken.getTokenType().name().toLowerCase())
                .build();
    }

    @Override
    public void initiateReset(String email) {
        User user = userRepository.findByEmail(email);
        // Invalidate any existing token
        verificationTokensRepository.deleteByUserAndType(user.getUserId(), TokenType.PASSWORD_RESET);

        String token = UUID.randomUUID().toString();
        VerificationToken resetToken = VerificationToken.builder()
                .token(token)
                .userId(user.getUserId())
                .tokenType(TokenType.PASSWORD_RESET)
                .expiresAt(LocalDateTime.now().plusHours(tokenExpiryHours))
                .build();
        verificationTokensRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), token);
    }

    @Override
    public void completeReset(String token, String newPassword) {
        VerificationToken resetToken = verificationTokensRepository.findByTokenAndType(token, TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            verificationTokensRepository.deleteByToken(resetToken.getToken());
            throw new ResourceInvalidException("Token has expired");
        }
        User user = userRepository.findByUserId(resetToken.getUserId());
        userRepository.updatePassword(user.getUserId(), passwordEncoder.encode(newPassword));
        verificationTokensRepository.deleteByToken(resetToken.getToken());
    }

    @Transactional
    @Override
    public void activateAccount(String rawToken, String newPassword) {
        log.info("Activating account for token: {}", rawToken);

        VerificationToken resetToken = verificationTokensRepository.findByTokenAndType(rawToken, TokenType.ACCOUNT_ACTIVATION)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        int rows = userRepository.activateUser(resetToken.getUserId(), passwordEncoder.encode(newPassword));
        if (rows > 0) {
            verificationTokensRepository.deleteByToken(resetToken.getToken());
            log.info("Account activated successfully for userId: {}", resetToken.getUserId());
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtConfig.getAuthExpirationMs())
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .storeId(user.getStoreId())
                .isActive(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

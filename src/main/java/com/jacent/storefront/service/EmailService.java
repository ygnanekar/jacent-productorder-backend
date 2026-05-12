package com.jacent.storefront.service;

public interface EmailService {
    void sendWelcomeEmail(String email, String username, String token);

    void sendPasswordResetEmail(String toEmail, String username, String token);
}
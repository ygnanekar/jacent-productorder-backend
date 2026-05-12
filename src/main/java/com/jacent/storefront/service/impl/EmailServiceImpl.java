package com.jacent.storefront.service.impl;

import com.jacent.storefront.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.name}")
    private String appName;

    @Value("${app.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${app.token-expiry-hours:24}")
    private int tokenExpiryHours;

    public EmailServiceImpl(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String username, String activationToken) {
        Context ctx = new Context();
        ctx.setVariable("username", username);
        ctx.setVariable("loginUrl", frontendBaseUrl + "/login");
        ctx.setVariable("activationLink", frontendBaseUrl + "/api/auth/activate?token=" + activationToken);
        ctx.setVariable("expiryHours",    tokenExpiryHours);
        sendHtml(toEmail, "Welcome to "+ appName +"!", "welcome", ctx);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        Context ctx = new Context();
        ctx.setVariable("username", username);
        ctx.setVariable("resetLink", frontendBaseUrl + "/reset-password?token=" + token);
        ctx.setVariable("expiryHours", 24);
        sendHtml(toEmail, appName + "-Reset Your Password", "reset-password", ctx);
    }

    private void sendHtml(String to, String subject, String template, Context ctx) {
        String html = templateEngine.process(template, ctx);
        MimeMessagePreparator prep = msg -> {
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
        };
        mailSender.send(prep);
    }
}
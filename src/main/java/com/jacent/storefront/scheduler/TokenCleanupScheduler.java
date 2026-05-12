package com.jacent.storefront.scheduler;

import com.jacent.storefront.repository.VerificationTokensRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupScheduler {

    private final VerificationTokensRepository verificationTokensRepository;

    public TokenCleanupScheduler(VerificationTokensRepository verificationTokensRepository) {
        this.verificationTokensRepository = verificationTokensRepository;
    }

    @Scheduled(cron = "0 30 0 * * ?", zone = "America/Chicago")
    public void purgeExpiredTokens() {
        verificationTokensRepository.deleteAllExpired();
    }
}

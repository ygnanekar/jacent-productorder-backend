package com.jacent.storefront.query;

import com.jacent.storefront.configuration.YamlPropertySourceFactory;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "verification-token.queries")
@PropertySource(value = "classpath:queries/verification-token-queries.yaml", factory = YamlPropertySourceFactory.class)
@Data
public class VerificationTokenQueries {
    private String saveToken;
    private String verificationTokenByToken;
    private String verificationTokenByTokenAndType;
    private String deleteVerificationTokenByToken;
    private String deleteVerificationTokenByUserIdAndTokenType;
    private String deleteAllExpiredTokens;
}

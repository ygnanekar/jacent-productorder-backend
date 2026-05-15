package com.jacent.storefront.repository;

import com.jacent.storefront.entity.TokenType;
import com.jacent.storefront.entity.VerificationToken;
import com.jacent.storefront.exception.ResourceAlreadyExistsException;
import com.jacent.storefront.exception.ResourceCreationException;
import com.jacent.storefront.query.VerificationTokenQueries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class VerificationTokensRepository {

    @Autowired
    VerificationTokenQueries verificationTokenQueries;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final RowMapper<VerificationToken> ROW_MAPPER = (rs, rowNum) ->
            VerificationToken.builder()
                    .id(rs.getLong("id"))
                    .token(rs.getString("token"))
                    .tokenType(TokenType.valueOf(rs.getString("token_type")))
                    .userId(rs.getString("user_id"))
                    .expiresAt(rs.getTimestamp("expires_at").toLocalDateTime())
                    .used(rs.getBoolean("used"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .build();


    public VerificationToken save(VerificationToken token) {
        log.info("Saving verification token for userId: {}, type: {}", token.getUserId(), token.getTokenType());
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(
                    verificationTokenQueries.getSaveToken(),
                    toParams(token),
                    keyHolder
            );

            long generatedId = keyHolder.getKey().longValue();
            token.setId(generatedId);

            log.info("Verification token saved successfully — id: {}, userId: {}, type: {}",
                    generatedId, token.getUserId(), token.getTokenType());

            return token;

        } catch (DuplicateKeyException e) {
            log.error("Duplicate token detected for userId: {}, type: {}", token.getUserId(), token.getTokenType(), e);
            throw new ResourceAlreadyExistsException(
                    "A token already exists for userId: " + token.getUserId() + ", type: " + token.getTokenType(), e);

        } catch (DataAccessException e) {
            log.error("Database error while saving token for userId: {}, type: {}", token.getUserId(), token.getTokenType(), e);
            throw new ResourceCreationException(
                    "Failed to save verification token for userId: " + token.getUserId(), e
            );
        }
    }

    public Optional<VerificationToken> findByTokenAndType(String token, TokenType tokenType) {
        log.debug("Looking up verification token: {}", token);
        try {
            Optional<VerificationToken> result = namedParameterJdbcTemplate
                    .query(
                            tokenType == null ? verificationTokenQueries.getVerificationTokenByToken() : verificationTokenQueries.getVerificationTokenByTokenAndType(),
                            Map.of("token", token, "tokenType", tokenType != null ? tokenType.name(): ""),
                            ROW_MAPPER
                    )
                    .stream()
                    .findFirst();

            if (result.isPresent()) {
                log.debug("Token found — type: {}, userId: {}, expired: {}",
                        result.get().getTokenType(), result.get().getUserId(), result.get().isExpired());
            } else {
                log.warn("No verification token found for token: {}", token);
            }

            return result;

        } catch (DataAccessException e) {
            log.error("Database error while fetching token: {}", token, e);
            throw new ResourceCreationException("Failed to fetch verification token", e);
        }
    }

    public void deleteByUserAndType(String userId, TokenType type) {
        log.info("Deleting verification tokens for userId: {}, type: {}", userId, type);
        try {
            int rows = namedParameterJdbcTemplate.update(
                    verificationTokenQueries.getDeleteVerificationTokenByUserIdAndTokenType(),
                    Map.of("userId", userId, "tokenType", type.name())
            );
            if (rows == 0) {
                log.warn("No tokens found to delete for userId: {}, type: {}", userId, type);
            } else {
                log.info("Deleted {} token(s) for userId: {}, type: {}", rows, userId, type);
            }
        } catch (DataAccessException e) {
            log.error("Database error while deleting tokens for userId: {}, type: {}", userId, type, e);
            throw new ResourceCreationException(
                    "Failed to delete tokens for userId: " + userId + ", type: " + type, e
            );
        }
    }

    public void deleteByToken(String token) {
        log.info("Deleting verification token: {}", token);
        try {
            int rows = namedParameterJdbcTemplate.update(
                    verificationTokenQueries.getDeleteVerificationTokenByToken(),
                    Map.of("token", token)
            );
            if (rows == 0) {
                log.warn("No token found to delete for token: {}", token);
            } else {
                log.info("Verification token deleted successfully: {}", token);
            }
        } catch (DataAccessException e) {
            log.error("Database error while deleting token: {}", token, e);
            throw new ResourceCreationException("Failed to delete verification token", e);
        }
    }

    public int deleteAllExpired() {
        log.info("Starting cleanup of expired verification tokens");
        try {
            int rows = namedParameterJdbcTemplate.update(verificationTokenQueries.getDeleteAllExpiredTokens(), Map.of("now", LocalDateTime.now()));
            if (rows == 0) {
                log.info("No expired verification tokens found to delete");
            } else {
                log.info("Deleted {} expired verification token(s)", rows);
            }
            return rows;
        } catch (DataAccessException e) {
            log.error("Database error while deleting expired verification tokens", e);
            throw new ResourceCreationException("Failed to delete expired verification tokens", e);
        }
    }

    private MapSqlParameterSource toParams(VerificationToken t) {
        return new MapSqlParameterSource()
                .addValue("token", t.getToken())
                .addValue("tokenType", t.getTokenType().name())
                .addValue("userId", t.getUserId())
                .addValue("expiresAt", Timestamp.valueOf(t.getExpiresAt()))
                .addValue("used", t.isUsed());
    }

}

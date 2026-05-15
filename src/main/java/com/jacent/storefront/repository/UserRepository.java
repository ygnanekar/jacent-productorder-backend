package com.jacent.storefront.repository;

import com.jacent.storefront.entity.User;
import com.jacent.storefront.exception.ResourceCreationException;
import com.jacent.storefront.exception.ResourceNotFoundException;
import com.jacent.storefront.exception.ResourceInundationException;
import com.jacent.storefront.query.UserQueries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import java.time.LocalDateTime;

@Slf4j
@Repository
public class UserRepository {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    UserQueries userQueries;

    public boolean existsByEmail(String email) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", email.trim().toLowerCase());

        try {
            Integer count = namedParameterJdbcTemplate.queryForObject(
                    userQueries.getEmailExists(),
                    params,
                    Integer.class
            );
            return count != null && count > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public User findByEmail(String email) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("email", email);
            return namedParameterJdbcTemplate.queryForObject(
                    userQueries.getUserByEmail(),
                    params,
                    new BeanPropertyRowMapper<>(User.class)
            );
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }

    public User findByUserId(String userId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("userId", userId);
            return namedParameterJdbcTemplate.queryForObject(
                    userQueries.getUserByUserId(),
                    params,
                    new BeanPropertyRowMapper<>(User.class)
            );
        } catch (EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User not found with userId: " + userId);
        }
    }

    public User createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());

        String userId = UUID.randomUUID().toString();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("firstName", user.getFirstName().trim());
        params.addValue("lastName", user.getLastName().trim());
        params.addValue("email", user.getEmail().toLowerCase().trim());
        params.addValue("password", user.getPassword());
        params.addValue("storeId", user.getStoreId());
        params.addValue("isEnabled", user.isEnabled() ? 1 : 0);
        params.addValue("isLocked", user.isLocked() ? 1 : 0);

        try {
            namedParameterJdbcTemplate.update(userQueries.getCreateUser(), params);
        } catch (DataIntegrityViolationException e) {
            log.warn("User creation failed - duplicate email: {}", user.getEmail());
            throw new ResourceCreationException("User with this email already exists", e);
        } catch (BadSqlGrammarException e) {
            log.error("User creation failed - SQL error", e);
            throw new ResourceCreationException("Invalid user data provided", e);
        } catch (Exception e) {
            log.error("User creation failed for email: {}", user.getEmail(), e);
            throw new ResourceCreationException("Failed to create user", e);
        }

        user.setUserId(userId);
        log.info("User created successfully with ID: {} and email: {}", userId, user.getEmail());

        return user;
    }

    public int activateUser(String userId, String encodedPassword) {
        log.info("Activating user with userId: {}", userId);
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("userId", userId)
                    .addValue("encodedPassword", encodedPassword)
                    .addValue("now",    LocalDateTime.now());

            int rows = namedParameterJdbcTemplate.update(userQueries.getActivateUser(), params);

            if (rows == 0) {
                log.warn("No inactive user found to activate for userId: {}", userId);
                throw new ResourceCreationException("User not found or already active for userId: " + userId);
            }

            log.info("User activated successfully for userId: {}", userId);
            return rows;

        } catch (ResourceCreationException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error while activating userId: {}", userId, e);
            throw new ResourceInundationException("Failed to activate user for userId: " + userId, e);
        }
    }

    public void updatePassword(String userId, String encodedPassword) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("encodedPassword", encodedPassword)
                    .addValue("now", LocalDateTime.now())
                    .addValue("userId", userId);

            int rows = namedParameterJdbcTemplate.update(userQueries.getUpdatePassword(), params);
            if (rows == 0) {
                throw new ResourceCreationException("No user found with id: " + userId);
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInundationException("Password update violated a database constraint", e);
        } catch (DataAccessException e) {
            throw new ResourceInundationException("Database error while updating password for userId: " + userId);
        }
    }

}

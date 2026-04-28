package com.jacent.storefront.repository;

import com.jacent.storefront.entity.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ConfigurationRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<AppConfig> findAll() {
        return jdbcTemplate.query(
                "SELECT name, value, type, description, created_at, updated_at FROM app_config",
                new BeanPropertyRowMapper<>(AppConfig.class)
        );
    }
}

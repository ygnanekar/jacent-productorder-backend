package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Division;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DivisionRepository {
    private final JdbcTemplate jdbcTemplate;

    public DivisionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Division> findAll() {
        return jdbcTemplate.query(
                "SELECT division_id, division, division_code FROM division",
                new BeanPropertyRowMapper<>(Division.class)
        );
    }
}

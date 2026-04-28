package com.jacent.storefront.repository;

import com.jacent.storefront.entity.Commodity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommodityRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommodityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Commodity> findAll() {
        return jdbcTemplate.query(
                "SELECT commodity_id, commodity, division_id FROM commodity",
                new BeanPropertyRowMapper<>(Commodity.class)
        );
    }
}

package com.jacent.storefront.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppConfig {
    private String name;
    private String value;
    private Type type;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    enum Type {
        STRING,
        INTEGER,
        BOOLEAN
    }
}

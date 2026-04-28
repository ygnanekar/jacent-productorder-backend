package com.jacent.storefront.entity;

public enum Configuration {
    PAGINATION_SIZE("pagination.max-size");

    private final String key;

    Configuration(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

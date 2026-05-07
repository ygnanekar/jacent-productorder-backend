package com.jacent.storefront.entity;

public enum Configuration {
    PAGINATION_SIZE("pagination.max-size"),
    ENABLE_FULL_TEXT_OPEN_SEARCH("enable-full-text-opensearch");

    private final String key;

    Configuration(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

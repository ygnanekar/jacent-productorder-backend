package com.jacent.storefront.service;

import com.jacent.storefront.entity.AppConfig;
import com.jacent.storefront.entity.Configuration;

import java.util.List;

public interface ConfigurationService {
    List<AppConfig> getAll();

    String getValueAsString(Configuration configuration, String defaultValue);

    Integer getValueAsInteger(Configuration configuration, Integer defaultValue);

    Boolean getValueAsBoolean(Configuration Configuration, Boolean defaultValue);

    // Force reload from DB without restart
    void reload();
}

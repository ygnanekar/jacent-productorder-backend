package com.jacent.storefront.controller;

import com.jacent.storefront.entity.AppConfig;
import com.jacent.storefront.service.ConfigurationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/configurations")
@RestController
public class ConfigurationController {

    private final ConfigurationService configurationService;

    ConfigurationController(ConfigurationService configurationService){
        this.configurationService = configurationService;
    }

    @GetMapping
    public ResponseEntity<List<AppConfig>> getAllConfigurations() {
        return ResponseEntity.ok(configurationService.getAll());
    }

    @PatchMapping("/reload")
    public ResponseEntity<?> reloadConfigurations() {
        configurationService.reload();
        return ResponseEntity.ok(true);
    }
}

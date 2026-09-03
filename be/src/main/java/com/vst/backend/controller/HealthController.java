package com.vst.backend.controller;

import com.vst.backend.dto.HealthResponse;
import com.vst.backend.service.HealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/api/v1/health")
    public HealthResponse health() {
        return HealthResponse.from(healthService.getHealthStatus());
    }
}

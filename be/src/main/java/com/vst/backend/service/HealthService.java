package com.vst.backend.service;

import com.vst.backend.model.HealthStatus;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public HealthStatus getHealthStatus() {
        return new HealthStatus("UP", Instant.now());
    }
}

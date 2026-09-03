package com.vst.backend.dto;

import com.vst.backend.model.HealthStatus;
import java.time.Instant;

/** API-facing shape for a health check — kept separate from the internal model. */
public record HealthResponse(String status, Instant checkedAt) {

    public static HealthResponse from(HealthStatus healthStatus) {
        return new HealthResponse(healthStatus.status(), healthStatus.checkedAt());
    }
}

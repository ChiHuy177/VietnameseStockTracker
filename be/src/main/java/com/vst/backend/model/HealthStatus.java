package com.vst.backend.model;

import java.time.Instant;

/** Result of a health check. */
public record HealthStatus(String status, Instant checkedAt) {
}

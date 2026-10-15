package com.vst.backend.dto.vnstock;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw shape returned by the Python vnstock microservice's {@code GET /listing} endpoint.
 * Deliberately mirrors the external JSON as-is (snake_case field name and all) — no domain
 * meaning added here, that happens in {@link com.vst.backend.mapper.StockListingMapper}.
 */
public record VnstockListingItemDto(
        String symbol,
        @JsonProperty("organ_name") String organName,
        String exchange) {
}

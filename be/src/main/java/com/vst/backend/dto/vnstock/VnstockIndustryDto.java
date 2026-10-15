package com.vst.backend.dto.vnstock;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Raw shape returned by the Python vnstock microservice's {@code GET /industries} endpoint.
 * One row per (symbol, ICB level) — vnstock returns all 4 levels (broad to narrow) per symbol in
 * the same response; {@link com.vst.backend.mapper.IndustryMapper} picks the level this codebase
 * uses.
 */
public record VnstockIndustryDto(
        String symbol,
        @JsonProperty("icb_level") Integer icbLevel,
        @JsonProperty("icb_code") String icbCode,
        @JsonProperty("icb_name") String icbName) {
}

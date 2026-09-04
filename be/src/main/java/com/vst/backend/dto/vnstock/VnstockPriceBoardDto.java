package com.vst.backend.dto.vnstock;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Raw shape returned by the Python vnstock microservice's {@code GET /price-board} endpoint.
 * Deliberately mirrors the external JSON as-is — translated to a domain model in
 * {@link com.vst.backend.mapper.PriceQuoteMapper}.
 */
public record VnstockPriceBoardDto(
        String symbol,
        Long time,
        @JsonProperty("close_price") BigDecimal closePrice,
        @JsonProperty("price_change") BigDecimal priceChange,
        @JsonProperty("percent_change") BigDecimal percentChange,
        @JsonProperty("volume_accumulated") Long volumeAccumulated) {
}

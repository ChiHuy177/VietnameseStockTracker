package com.vst.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Field-for-field match to the frontend's {@code IndexQuote} model (fe/src/models/marketIndex.ts). */
public record IndexQuoteResponse(
        String symbol,
        String name,
        BigDecimal value,
        BigDecimal change,
        BigDecimal percentChange,
        Instant time) {
}

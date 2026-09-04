package com.vst.backend.model;

import java.math.BigDecimal;
import java.time.Instant;

/** One real-time price tick for a stock, ready to broadcast over WebSocket. */
public record PriceQuote(
        String symbol,
        Instant time,
        BigDecimal price,
        BigDecimal change,
        BigDecimal percentChange,
        long volume) {
}

package com.vst.backend.model;

import java.math.BigDecimal;
import java.time.Instant;

/** One OHLCV bar for one stock, ready to persist into {@code price_history_ohlcv}. */
public record OhlcvBar(
        long stockId,
        Instant time,
        String interval,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume,
        String source) {
}

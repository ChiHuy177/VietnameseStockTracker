package com.vst.backend.dto;

import com.vst.backend.model.OhlcvBar;
import java.math.BigDecimal;
import java.time.Instant;

/** One OHLCV bar, API-facing shape (no stock_id/source — internal detail, not useful to clients). */
public record OhlcvBarResponse(
        Instant time,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume) {

    public static OhlcvBarResponse from(OhlcvBar bar) {
        return new OhlcvBarResponse(bar.time(), bar.open(), bar.high(), bar.low(), bar.close(), bar.volume());
    }
}

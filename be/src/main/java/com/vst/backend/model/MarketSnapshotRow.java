package com.vst.backend.model;

import java.math.BigDecimal;

/** One stock's row in a daily full-market snapshot (see {@code daily_market_snapshot}). */
public record MarketSnapshotRow(
        long stockId,
        BigDecimal price,
        BigDecimal change,
        BigDecimal percentChange,
        BigDecimal totalValue,
        BigDecimal ceilingPrice,
        BigDecimal floorPrice) {
}

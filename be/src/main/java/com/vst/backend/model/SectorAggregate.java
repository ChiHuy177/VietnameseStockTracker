package com.vst.backend.model;

import java.math.BigDecimal;

/**
 * 1 ICB sector's aggregated figures for 1 trading day, built from {@code daily_market_snapshot}.
 * No real market-cap data exists from vnstock, so {@code marketCapWeight} is actually a turnover
 * share (see {@link com.vst.backend.service.SectorAnalyticsService}) — a documented heuristic, not
 * true market capitalization. {@code moneyFlowRaw} is in raw VND; the service divides by 1e9
 * ("tỷ đồng") when building the API response.
 */
public record SectorAggregate(String code, String name, BigDecimal marketCapWeight, BigDecimal percentChange, BigDecimal moneyFlowRaw) {
}

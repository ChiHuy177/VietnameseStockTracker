package com.vst.backend.dto;

import java.math.BigDecimal;

/**
 * Field-for-field match to the frontend's {@code SectorSnapshot} model (fe/src/models/sector.ts).
 *
 * <p>{@code marketCapWeight} is a turnover share, not true market capitalization — vnstock doesn't
 * expose shares-outstanding on any endpoint surveyed so far. {@code moneyFlow} is a heuristic
 * ({@code turnover × sign(percentChange)}), not real order-flow data. Both documented here because
 * the field names alone read as more precise than they are — see {@code SectorAnalyticsService}.
 */
public record SectorSnapshotResponse(
        String code,
        String name,
        BigDecimal marketCapWeight,
        BigDecimal percentChange,
        BigDecimal moneyFlow) {
}

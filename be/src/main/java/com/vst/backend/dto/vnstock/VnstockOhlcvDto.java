package com.vst.backend.dto.vnstock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Raw shape returned by the Python vnstock microservice's {@code GET /ohlcv} endpoint.
 * Deliberately mirrors the external JSON as-is — no domain meaning (stock id, source) added here,
 * that happens in {@link com.vst.backend.mapper.OhlcvMapper}.
 */
public record VnstockOhlcvDto(
        LocalDateTime time,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long volume) {
}

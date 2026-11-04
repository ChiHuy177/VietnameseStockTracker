package com.vst.backend.dto;

import java.time.Instant;

/** Field-for-field match to the frontend's {@code MarketBreadthSnapshot} model (fe/src/models/marketBreadth.ts). */
public record MarketBreadthResponse(
        int advancing,
        int declining,
        int unchanged,
        int ceiling,
        int floor,
        Instant time) {
}

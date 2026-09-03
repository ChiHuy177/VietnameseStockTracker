package com.vst.backend.dto;

import com.vst.backend.model.WatchlistEntry;
import java.time.Instant;

public record WatchlistItemResponse(String symbol, String name, String exchange, Instant addedAt) {

    public static WatchlistItemResponse from(WatchlistEntry entry) {
        return new WatchlistItemResponse(entry.symbol(), entry.name(), entry.exchange(), entry.addedAt());
    }
}

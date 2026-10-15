package com.vst.backend.model;

import java.time.Instant;

/** One stock on the (single, global) watchlist. */
public record WatchlistEntry(String symbol, String name, String exchange, Instant addedAt) {
}

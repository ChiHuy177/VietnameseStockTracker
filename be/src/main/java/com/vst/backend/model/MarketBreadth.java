package com.vst.backend.model;

/** Counts of advancing/declining/unchanged/ceiling/floor stocks for 1 trading day's snapshot. */
public record MarketBreadth(int advancing, int declining, int unchanged, int ceiling, int floor) {
}

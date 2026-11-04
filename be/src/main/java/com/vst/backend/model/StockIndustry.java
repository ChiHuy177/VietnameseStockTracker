package com.vst.backend.model;

/** A stock's ICB sector classification (level 2 — e.g. "Ngân hàng", "Bất động sản"). */
public record StockIndustry(String symbol, String icbCode, String icbName) {
}

package com.vst.backend.model;

/** One entry from the vnstock symbol listing: a symbol's company name and exchange. */
public record StockListing(String symbol, String name, String exchange) {
}

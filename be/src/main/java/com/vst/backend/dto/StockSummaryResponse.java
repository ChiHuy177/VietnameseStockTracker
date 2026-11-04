package com.vst.backend.dto;

import com.vst.backend.model.StockListing;

public record StockSummaryResponse(String symbol, String name, String exchange) {

    public static StockSummaryResponse from(StockListing listing) {
        return new StockSummaryResponse(listing.symbol(), listing.name(), listing.exchange());
    }
}

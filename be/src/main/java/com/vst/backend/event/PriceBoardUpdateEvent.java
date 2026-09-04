package com.vst.backend.event;

import com.vst.backend.model.PriceQuote;
import java.util.List;

/** Published whenever a fresh price board poll completes; broadcast to WebSocket clients. */
public record PriceBoardUpdateEvent(List<PriceQuote> quotes) {
}

package com.vst.backend.controller;

import com.vst.backend.dto.IndexQuoteResponse;
import com.vst.backend.dto.OhlcvHistoryResponse;
import com.vst.backend.service.IndexQuoteService;
import com.vst.backend.service.OhlcvQueryService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Market indices (VNINDEX/VN30/HNX-INDEX/UPCOM-INDEX) — kept separate from {@link StockController}
 * even though they're stored as {@code stocks} rows: indices aren't searchable, aren't
 * watchlist-able, and have no "current quote" concept for regular stocks to share.
 */
@RestController
@RequestMapping("/api/v1/indices")
public class IndexController {

    private final IndexQuoteService indexQuoteService;
    private final OhlcvQueryService ohlcvQueryService;

    public IndexController(IndexQuoteService indexQuoteService, OhlcvQueryService ohlcvQueryService) {
        this.indexQuoteService = indexQuoteService;
        this.ohlcvQueryService = ohlcvQueryService;
    }

    @GetMapping
    public List<IndexQuoteResponse> list() {
        return indexQuoteService.getCurrentQuotes();
    }

    @GetMapping("/{symbol}/ohlcv")
    public OhlcvHistoryResponse getOhlcv(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ohlcvQueryService.getHistory(symbol, start, end);
    }
}

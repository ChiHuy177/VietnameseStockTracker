package com.vst.backend.controller;

import com.vst.backend.dto.OhlcvHistoryResponse;
import com.vst.backend.dto.StockSummaryResponse;
import com.vst.backend.service.OhlcvQueryService;
import com.vst.backend.service.StockSearchService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {

    private final OhlcvQueryService ohlcvQueryService;
    private final StockSearchService stockSearchService;

    public StockController(OhlcvQueryService ohlcvQueryService, StockSearchService stockSearchService) {
        this.ohlcvQueryService = ohlcvQueryService;
        this.stockSearchService = stockSearchService;
    }

    @GetMapping("/{symbol}/ohlcv")
    public OhlcvHistoryResponse getOhlcv(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ohlcvQueryService.getHistory(symbol, start, end);
    }

    @GetMapping
    public List<StockSummaryResponse> search(@RequestParam(required = false, defaultValue = "") String q) {
        return stockSearchService.search(q);
    }
}

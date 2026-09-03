package com.vst.backend.controller;

import com.vst.backend.dto.OhlcvHistoryResponse;
import com.vst.backend.service.OhlcvQueryService;
import java.time.LocalDate;
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

    public StockController(OhlcvQueryService ohlcvQueryService) {
        this.ohlcvQueryService = ohlcvQueryService;
    }

    @GetMapping("/{symbol}/ohlcv")
    public OhlcvHistoryResponse getOhlcv(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ohlcvQueryService.getHistory(symbol, start, end);
    }
}

package com.vst.backend.controller;

import com.vst.backend.dto.MarketBreadthResponse;
import com.vst.backend.dto.SectorSnapshotResponse;
import com.vst.backend.service.MarketBreadthService;
import com.vst.backend.service.SectorAnalyticsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Whole-market aggregates (breadth, sector treemap/money-flow) — kept separate from
 * {@link StockController}/{@link IndexController}: these aren't per-symbol reads, they're derived
 * from the daily full-market snapshot ({@code daily_market_snapshot}).
 */
@RestController
@RequestMapping("/api/v1/market")
public class MarketController {

    private final MarketBreadthService marketBreadthService;
    private final SectorAnalyticsService sectorAnalyticsService;

    public MarketController(MarketBreadthService marketBreadthService, SectorAnalyticsService sectorAnalyticsService) {
        this.marketBreadthService = marketBreadthService;
        this.sectorAnalyticsService = sectorAnalyticsService;
    }

    @GetMapping("/breadth")
    public MarketBreadthResponse getBreadth() {
        return marketBreadthService.getBreadth();
    }

    @GetMapping("/sectors")
    public List<SectorSnapshotResponse> getSectors() {
        return sectorAnalyticsService.getSectorSnapshots();
    }
}

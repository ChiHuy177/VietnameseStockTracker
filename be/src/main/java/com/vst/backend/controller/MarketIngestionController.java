package com.vst.backend.controller;

import com.vst.backend.service.IndustrySyncService;
import com.vst.backend.service.MarketSnapshotIngestionService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Trigger tay cho {@link IndustrySyncService}/{@link MarketSnapshotIngestionService} — chỉ tồn tại
 * ở profile {@code dev}, cùng lý do và cùng pattern với {@link IndexIngestionController}: cả 2 job
 * chỉ chạy theo cron, nên trên một DB mới {@code /api/v1/market/breadth}/{@code /sectors} không có
 * gì để trả cho tới phiên cron đầu tiên.
 */
@RestController
@RequestMapping("/api/v1/market")
@Profile("dev")
public class MarketIngestionController {

    private final IndustrySyncService industrySyncService;
    private final MarketSnapshotIngestionService marketSnapshotIngestionService;

    public MarketIngestionController(
            IndustrySyncService industrySyncService, MarketSnapshotIngestionService marketSnapshotIngestionService) {
        this.industrySyncService = industrySyncService;
        this.marketSnapshotIngestionService = marketSnapshotIngestionService;
    }

    @PostMapping("/industries/sync")
    public ResponseEntity<Void> syncIndustries() {
        industrySyncService.syncAll();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/snapshot/ingest")
    public ResponseEntity<Void> ingestSnapshot() {
        marketSnapshotIngestionService.ingestAll();
        return ResponseEntity.noContent().build();
    }
}

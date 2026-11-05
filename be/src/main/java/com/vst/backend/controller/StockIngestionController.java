package com.vst.backend.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vst.backend.service.OhlcvIngestionService;

/**
 * Trigger tay cho {@link OhlcvIngestionService} — chỉ tồn tại ở profile {@code dev}.
 *
 * <p>Cùng pattern với {@link IndexIngestionController}: cron mặc định chạy lúc 18h nên khi dev/test
 * có thể gọi endpoint này để nạp hoặc bù (catch-up) dữ liệu nến ngay lập tức.
 */
@RestController
@RequestMapping("/api/v1/stocks")
@Profile("dev")
public class StockIngestionController {

    private final OhlcvIngestionService ohlcvIngestionService;

    public StockIngestionController(OhlcvIngestionService ohlcvIngestionService) {
        this.ohlcvIngestionService = ohlcvIngestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest() {
        ohlcvIngestionService.ingestAll();
        return ResponseEntity.noContent().build();
    }
}


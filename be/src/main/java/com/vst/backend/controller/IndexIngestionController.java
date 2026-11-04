package com.vst.backend.controller;

import com.vst.backend.service.IndexIngestionService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Trigger tay cho {@link IndexIngestionService} — chỉ tồn tại ở profile {@code dev}.
 *
 * <p>Lý do: ingestion chỉ chạy theo cron ({@code 0 10 18 * * MON-FRI}), nên trên một DB mới
 * {@code GET /api/v1/indices} không có gì để trả cho tới phiên cron đầu tiên — lúc dev/demo thì
 * không chờ được. Chạy đồng bộ (không {@code @Async}) để gọi xong là có dữ liệu ngay: chỉ 4 chỉ số
 * nên request treo vài giây là chấp nhận được, và không bật ở prod.
 */
@RestController
@RequestMapping("/api/v1/indices")
@Profile("dev")
public class IndexIngestionController {

    private final IndexIngestionService indexIngestionService;

    public IndexIngestionController(IndexIngestionService indexIngestionService) {
        this.indexIngestionService = indexIngestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest() {
        indexIngestionService.ingestAll();
        return ResponseEntity.noContent().build();
    }
}

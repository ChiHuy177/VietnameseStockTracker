package com.vst.backend.service;

import com.vst.backend.dto.MarketBreadthResponse;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.MarketBreadth;
import com.vst.backend.repository.MarketSnapshotRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;

/**
 * Reads the latest day's {@code daily_market_snapshot} (written by
 * {@link MarketSnapshotIngestionService}) and counts advancing/declining/unchanged/ceiling/floor.
 */
@Service
public class MarketBreadthService {

    private final MarketSnapshotRepository marketSnapshotRepository;

    public MarketBreadthService(MarketSnapshotRepository marketSnapshotRepository) {
        this.marketSnapshotRepository = marketSnapshotRepository;
    }

    public MarketBreadthResponse getBreadth() {
        LocalDate date = marketSnapshotRepository.findLatestSnapshotDate()
                .orElseThrow(() -> new ResourceNotFoundException("No market snapshot ingested yet"));

        MarketBreadth breadth = marketSnapshotRepository.findBreadth(date);

        return new MarketBreadthResponse(
                breadth.advancing(),
                breadth.declining(),
                breadth.unchanged(),
                breadth.ceiling(),
                breadth.floor(),
                date.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}

package com.vst.backend.service;

import com.vst.backend.dto.SectorSnapshotResponse;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.SectorAggregate;
import com.vst.backend.repository.MarketSnapshotRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Reads the latest day's {@code daily_market_snapshot} and aggregates it by ICB sector — backs
 * both the treemap (all sectors) and the money-flow panel (FE sorts/slices the same list by
 * {@code |moneyFlow|}, same pattern as the mock data it replaces).
 */
@Service
public class SectorAnalyticsService {

    private static final BigDecimal VND_PER_BILLION = BigDecimal.valueOf(1_000_000_000L);

    private final MarketSnapshotRepository marketSnapshotRepository;

    public SectorAnalyticsService(MarketSnapshotRepository marketSnapshotRepository) {
        this.marketSnapshotRepository = marketSnapshotRepository;
    }

    public List<SectorSnapshotResponse> getSectorSnapshots() {
        LocalDate date = marketSnapshotRepository.findLatestSnapshotDate()
                .orElseThrow(() -> new ResourceNotFoundException("No market snapshot ingested yet"));

        return marketSnapshotRepository.findSectorAggregates(date).stream()
                .map(this::toResponse)
                .toList();
    }

    private SectorSnapshotResponse toResponse(SectorAggregate aggregate) {
        return new SectorSnapshotResponse(
                aggregate.code(),
                aggregate.name(),
                aggregate.marketCapWeight().setScale(1, RoundingMode.HALF_UP),
                aggregate.percentChange().setScale(2, RoundingMode.HALF_UP),
                aggregate.moneyFlowRaw().divide(VND_PER_BILLION, 0, RoundingMode.HALF_UP));
    }
}

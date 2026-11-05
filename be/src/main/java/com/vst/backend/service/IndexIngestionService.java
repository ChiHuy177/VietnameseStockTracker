package com.vst.backend.service;

import com.vst.backend.dto.vnstock.VnstockOhlcvDto;
import com.vst.backend.mapper.OhlcvMapper;
import com.vst.backend.model.OhlcvBar;
import com.vst.backend.repository.PriceHistoryRepository;
import com.vst.backend.repository.StockRepository;
import com.vst.backend.repository.VnstockClient;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Sibling of {@link OhlcvIngestionService}, scoped to the 4 market indices instead of tradeable
 * stocks. Reuses {@link OhlcvMapper} and {@link PriceHistoryRepository} unchanged — the index
 * OHLCV call returns the exact same shape as the equity one, just via a different vnstock/Python
 * endpoint (indices aren't understood by the equity endpoint).
 *
 * <p>Uses incremental sync: checks the latest existing bar in the DB for each index so missed
 * days (e.g. server downtime) are automatically backfilled, while daily runs only pull recent bars.
 */
@Service
public class IndexIngestionService {

    private static final Logger log = LoggerFactory.getLogger(IndexIngestionService.class);
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final VnstockClient vnstockClient;
    private final OhlcvMapper mapper;
    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final long lookbackDays;

    public IndexIngestionService(
            VnstockClient vnstockClient,
            OhlcvMapper mapper,
            StockRepository stockRepository,
            PriceHistoryRepository priceHistoryRepository,
            @Value("${index-ingestion.lookback-days}") long lookbackDays) {
        this.vnstockClient = vnstockClient;
        this.mapper = mapper;
        this.stockRepository = stockRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.lookbackDays = lookbackDays;
    }

    @Scheduled(cron = "${index-ingestion.cron}")
    public void ingestAll() {
        LocalDate today = LocalDate.now(VN_ZONE);
        List<String> symbols = stockRepository.findIndexSymbols();
        log.info("Starting index OHLCV ingestion for {} indices", symbols.size());

        Map<Long, LocalDate> latestDates = priceHistoryRepository.findLatestDates();

        for (String symbol : symbols) {
            try {
                long stockId = stockRepository.findOrCreateBySymbol(symbol);
                LocalDate latest = latestDates.get(stockId);
                LocalDate start = (latest != null) ? latest : today.minusDays(lookbackDays);
                LocalDate end = today;

                if (start.isAfter(end)) {
                    start = end;
                }

                ingestOne(stockId, symbol, start, end);
            } catch (Exception e) {
                log.error("Index ingestion failed for {}", symbol, e);
            }
        }

        log.info("Index OHLCV ingestion cycle complete: {} indices processed", symbols.size());
    }

    private void ingestOne(long stockId, String symbol, LocalDate start, LocalDate end) {
        List<VnstockOhlcvDto> dtos = vnstockClient.fetchIndexOhlcv(symbol, start.toString(), end.toString());
        List<OhlcvBar> bars = dtos.stream().map(dto -> mapper.toDomain(stockId, symbol, dto)).toList();

        priceHistoryRepository.upsertAll(bars);
        log.info("Ingested {} bars for index {} (from {} to {})", bars.size(), symbol, start, end);
    }
}

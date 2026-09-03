package com.vst.backend.service;

import com.vst.backend.dto.vnstock.VnstockOhlcvDto;
import com.vst.backend.mapper.OhlcvMapper;
import com.vst.backend.model.OhlcvBar;
import com.vst.backend.repository.PriceHistoryRepository;
import com.vst.backend.repository.StockRepository;
import com.vst.backend.repository.VnstockClient;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Periodically fetches OHLCV data (via {@link VnstockClient}) for a fixed watchlist and upserts
 * it into {@code price_history_ohlcv}. One symbol failing does not stop the others — this method
 * runs outside any HTTP request, so GlobalExceptionHandler never sees exceptions thrown here.
 */
@Service
public class OhlcvIngestionService {

    private static final Logger log = LoggerFactory.getLogger(OhlcvIngestionService.class);

    private final VnstockClient vnstockClient;
    private final OhlcvMapper mapper;
    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final List<String> symbols;
    private final long lookbackDays;

    public OhlcvIngestionService(
            VnstockClient vnstockClient,
            OhlcvMapper mapper,
            StockRepository stockRepository,
            PriceHistoryRepository priceHistoryRepository,
            @Value("${ingestion.symbols}") String symbolsCsv,
            @Value("${ingestion.lookback-days}") long lookbackDays) {
        this.vnstockClient = vnstockClient;
        this.mapper = mapper;
        this.stockRepository = stockRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.symbols = List.of(symbolsCsv.split(","));
        this.lookbackDays = lookbackDays;
    }

    @Scheduled(cron = "${ingestion.cron}")
    public void ingestAll() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(lookbackDays);

        for (String symbol : symbols) {
            try {
                ingestOne(symbol.strip(), start, end);
            } catch (Exception e) {
                log.error("Ingestion failed for symbol {}", symbol, e);
            }
        }
    }

    private void ingestOne(String symbol, LocalDate start, LocalDate end) {
        long stockId = stockRepository.findOrCreateBySymbol(symbol);
        List<VnstockOhlcvDto> dtos = vnstockClient.fetchOhlcv(symbol, start.toString(), end.toString());
        List<OhlcvBar> bars = dtos.stream().map(dto -> mapper.toDomain(stockId, symbol, dto)).toList();

        priceHistoryRepository.upsertAll(bars);
        log.info("Ingested {} bars for {}", bars.size(), symbol);
    }
}

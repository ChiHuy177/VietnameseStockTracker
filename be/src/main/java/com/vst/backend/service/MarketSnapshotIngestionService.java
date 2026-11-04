package com.vst.backend.service;

import com.vst.backend.dto.vnstock.VnstockPriceBoardDto;
import com.vst.backend.mapper.PriceQuoteMapper;
import com.vst.backend.model.MarketSnapshotRow;
import com.vst.backend.model.PriceQuote;
import com.vst.backend.repository.MarketSnapshotRepository;
import com.vst.backend.repository.StockRepository;
import com.vst.backend.repository.VnstockClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Daily full-market snapshot backing breadth (advancing/declining/unchanged) and the sector
 * treemap/money-flow widgets — deliberately separate from {@link PriceBoardPollerService}, which
 * stays scoped to the watchlist. 1 batch {@code price_board} call for every known stock (verified
 * manually: ~1500 symbols in one request, ~4s, no chunking needed), not a per-symbol loop.
 *
 * <p>Unlike the watchlist poller, 1 bad row here is isolated instead of failing the whole cycle —
 * losing a single stock's quote is cheap, but this job only runs once a day, so losing the entire
 * snapshot to 1 malformed row would be far more costly than it is for a 3s poll that just retries
 * next cycle.
 */
@Service
public class MarketSnapshotIngestionService {

    private static final Logger log = LoggerFactory.getLogger(MarketSnapshotIngestionService.class);

    private final VnstockClient vnstockClient;
    private final PriceQuoteMapper mapper;
    private final StockRepository stockRepository;
    private final MarketSnapshotRepository marketSnapshotRepository;

    public MarketSnapshotIngestionService(
            VnstockClient vnstockClient,
            PriceQuoteMapper mapper,
            StockRepository stockRepository,
            MarketSnapshotRepository marketSnapshotRepository) {
        this.vnstockClient = vnstockClient;
        this.mapper = mapper;
        this.stockRepository = stockRepository;
        this.marketSnapshotRepository = marketSnapshotRepository;
    }

    @Scheduled(cron = "${market-snapshot.cron}")
    public void ingestAll() {
        List<String> symbols = stockRepository.findAllSymbols();
        if (symbols.isEmpty()) {
            return;
        }

        List<PriceQuote> quotes;
        try {
            quotes = fetchQuotes(symbols);
        } catch (Exception e) {
            log.error("Market snapshot fetch failed", e);
            return;
        }

        // close_price == 0 means the symbol simply didn't trade this session (illiquid/suspended) —
        // not a real quote. Left in, it would wrongly inflate the "unchanged" breadth bucket.
        List<PriceQuote> traded = quotes.stream().filter(q -> q.price().signum() != 0).toList();

        Map<String, Long> idsBySymbol =
                stockRepository.findIdsBySymbols(traded.stream().map(PriceQuote::symbol).toList());

        List<MarketSnapshotRow> rows = new ArrayList<>(traded.size());
        for (PriceQuote quote : traded) {
            Long stockId = idsBySymbol.get(quote.symbol());
            if (stockId == null) {
                continue;
            }
            rows.add(new MarketSnapshotRow(
                    stockId, quote.price(), quote.change(), quote.percentChange(),
                    quote.totalValue(), quote.ceilingPrice(), quote.floorPrice()));
        }

        marketSnapshotRepository.upsertAll(rows, LocalDate.now());
        log.info("Market snapshot ingested: {} symbols fetched, {} traded, {} rows stored",
                quotes.size(), traded.size(), rows.size());
    }

    private List<PriceQuote> fetchQuotes(List<String> symbols) {
        List<VnstockPriceBoardDto> dtos = vnstockClient.fetchPriceBoard(symbols);
        List<PriceQuote> quotes = new ArrayList<>(dtos.size());
        for (VnstockPriceBoardDto dto : dtos) {
            try {
                quotes.add(mapper.toDomain(dto));
            } catch (Exception e) {
                log.warn("Skipping 1 symbol in market snapshot: {}", e.getMessage());
            }
        }
        return quotes;
    }
}

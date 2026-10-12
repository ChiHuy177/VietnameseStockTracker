package com.vst.backend.service;

import com.vst.backend.dto.IndexQuoteResponse;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.OhlcvBar;
import com.vst.backend.model.StockListing;
import com.vst.backend.repository.PriceHistoryRepository;
import com.vst.backend.repository.StockRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * vnstock has no "current index value" call — only historical OHLCV (see
 * {@code IndexIngestionService}). A "current quote" (value/change/percentChange) has to be
 * derived here from the latest 2 ingested bars, which is a computation nothing else in this
 * codebase needs (stocks get their real-time quote from the Python price-board instead).
 */
@Service
public class IndexQuoteService {

    private static final int PERCENT_SCALE = 2;

    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public IndexQuoteService(StockRepository stockRepository, PriceHistoryRepository priceHistoryRepository) {
        this.stockRepository = stockRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    public List<IndexQuoteResponse> getCurrentQuotes() {
        return stockRepository.findIndices().stream().map(this::toQuote).toList();
    }

    private IndexQuoteResponse toQuote(StockListing index) {
        long stockId = stockRepository.findBySymbol(index.symbol())
                .orElseThrow(() -> new ResourceNotFoundException("Stock", index.symbol()));

        List<OhlcvBar> latestTwo = priceHistoryRepository.findLatestTwoBars(stockId);
        if (latestTwo.isEmpty()) {
            throw new ResourceNotFoundException("No price history ingested yet for index " + index.symbol());
        }

        OhlcvBar latest = latestTwo.get(0);
        BigDecimal value = latest.close();

        // Day 1 after rollout: only 1 bar exists yet, nothing to compare against.
        if (latestTwo.size() < 2) {
            return new IndexQuoteResponse(index.symbol(), index.name(), value, BigDecimal.ZERO, BigDecimal.ZERO, latest.time());
        }

        BigDecimal previousClose = latestTwo.get(1).close();
        BigDecimal change = value.subtract(previousClose);
        BigDecimal percentChange = previousClose.signum() == 0
                ? BigDecimal.ZERO
                : change.divide(previousClose, PERCENT_SCALE + 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(PERCENT_SCALE, RoundingMode.HALF_UP);

        return new IndexQuoteResponse(index.symbol(), index.name(), value, change, percentChange, latest.time());
    }
}

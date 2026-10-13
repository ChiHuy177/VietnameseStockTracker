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
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * vnstock has no "current index value" call — only historical OHLCV (see
 * {@code IndexIngestionService}). A "current quote" (value/change/percentChange) has to be
 * derived here from the latest 2 ingested bars, which is a computation nothing else in this
 * codebase needs (stocks get their real-time quote from the Python price-board instead).
 */
@Service
public class IndexQuoteService {

    private static final Logger log = LoggerFactory.getLogger(IndexQuoteService.class);

    private static final int PERCENT_SCALE = 2;

    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public IndexQuoteService(StockRepository stockRepository, PriceHistoryRepository priceHistoryRepository) {
        this.stockRepository = stockRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    /**
     * Chỉ số chưa có bar nào thì bị bỏ qua, không làm fail cả list — ingestion chạy theo cron nên
     * một chỉ số mới seed (hoặc vnstock lỗi đúng mã đó) là trạng thái bình thường, không phải lỗi
     * của request. Chỉ khi **không** chỉ số nào có dữ liệu mới là 404: lúc đó job ingest chưa chạy
     * phiên nào.
     */
    public List<IndexQuoteResponse> getCurrentQuotes() {
        List<StockListing> indices = stockRepository.findIndices();
        List<IndexQuoteResponse> quotes = indices.stream()
                .map(this::toQuoteOrNull)
                .filter(Objects::nonNull)
                .toList();

        if (quotes.isEmpty() && !indices.isEmpty()) {
            throw new ResourceNotFoundException("No price history ingested yet for any market index");
        }

        return quotes;
    }

    private IndexQuoteResponse toQuoteOrNull(StockListing index) {
        long stockId = stockRepository.findBySymbol(index.symbol())
                .orElseThrow(() -> new ResourceNotFoundException("Stock", index.symbol()));

        List<OhlcvBar> latestTwo = priceHistoryRepository.findLatestTwoBars(stockId);
        if (latestTwo.isEmpty()) {
            log.warn("No price history for index {} yet, leaving it out of the quote list", index.symbol());
            return null;
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

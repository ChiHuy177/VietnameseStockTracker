package com.vst.backend.service;

import com.vst.backend.dto.OhlcvBarResponse;
import com.vst.backend.dto.OhlcvHistoryResponse;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.OhlcvBar;
import com.vst.backend.repository.PriceHistoryRepository;
import com.vst.backend.repository.StockRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OhlcvQueryService {

    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public OhlcvQueryService(StockRepository stockRepository, PriceHistoryRepository priceHistoryRepository) {
        this.stockRepository = stockRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    public OhlcvHistoryResponse getHistory(String symbol, LocalDate start, LocalDate end) {
        String normalizedSymbol = symbol.strip().toUpperCase();
        long stockId = stockRepository.findBySymbol(normalizedSymbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", normalizedSymbol));

        List<OhlcvBar> bars = priceHistoryRepository.findByStockIdAndRange(stockId, start, end);
        List<OhlcvBarResponse> barResponses = bars.stream().map(OhlcvBarResponse::from).toList();

        return new OhlcvHistoryResponse(normalizedSymbol, barResponses);
    }
}

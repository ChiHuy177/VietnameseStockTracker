package com.vst.backend.service;

import com.vst.backend.dto.StockSummaryResponse;
import com.vst.backend.exception.ValidationException;
import com.vst.backend.repository.StockRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StockSearchService {

    private static final int MAX_RESULTS = 20;

    private final StockRepository stockRepository;

    public StockSearchService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public List<StockSummaryResponse> search(String query) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Search query must not be blank");
        }
        return stockRepository.search(query.strip(), MAX_RESULTS).stream()
                .map(StockSummaryResponse::from)
                .toList();
    }
}

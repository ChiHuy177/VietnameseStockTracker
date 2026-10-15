package com.vst.backend.service;

import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.WatchlistEntry;
import com.vst.backend.repository.StockRepository;
import com.vst.backend.repository.WatchlistRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WatchlistService {

    private final StockRepository stockRepository;
    private final WatchlistRepository watchlistRepository;

    public WatchlistService(StockRepository stockRepository, WatchlistRepository watchlistRepository) {
        this.stockRepository = stockRepository;
        this.watchlistRepository = watchlistRepository;
    }

    public List<WatchlistEntry> list() {
        return watchlistRepository.findAll();
    }

    /** Idempotent: adding a symbol already on the watchlist just returns its current entry. */
    public WatchlistEntry add(String symbol) {
        String normalized = normalize(symbol);
        long stockId = stockRepository.findOrCreateBySymbol(normalized);
        watchlistRepository.add(stockId);
        return watchlistRepository.findByStockId(stockId)
                .orElseThrow(() -> new IllegalStateException("Watchlist entry missing right after insert"));
    }

    public void remove(String symbol) {
        String normalized = normalize(symbol);
        long stockId = stockRepository.findBySymbol(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", normalized));
        if (!watchlistRepository.remove(stockId)) {
            throw new ResourceNotFoundException("WatchlistItem", normalized);
        }
    }

    private static String normalize(String symbol) {
        return symbol.strip().toUpperCase();
    }
}

package com.vst.backend.service;

import com.vst.backend.dto.vnstock.VnstockListingItemDto;
import com.vst.backend.mapper.StockListingMapper;
import com.vst.backend.model.StockListing;
import com.vst.backend.repository.StockRepository;
import com.vst.backend.repository.VnstockClient;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Periodically refreshes company name + exchange metadata for every listed symbol, so the search
 * endpoint has something to match against. Runs far less often than OHLCV ingestion since listings
 * change rarely (new IPOs, delistings) — see {@code listing-sync.cron}.
 */
@Service
public class StockListingSyncService {

    private static final Logger log = LoggerFactory.getLogger(StockListingSyncService.class);

    private final VnstockClient vnstockClient;
    private final StockListingMapper mapper;
    private final StockRepository stockRepository;

    public StockListingSyncService(VnstockClient vnstockClient, StockListingMapper mapper, StockRepository stockRepository) {
        this.vnstockClient = vnstockClient;
        this.mapper = mapper;
        this.stockRepository = stockRepository;
    }

    @Scheduled(cron = "${listing-sync.cron}")
    public void syncAll() {
        try {
            List<VnstockListingItemDto> dtos = vnstockClient.fetchListing();
            List<StockListing> listing = dtos.stream().map(mapper::toDomain).toList();
            stockRepository.upsertListing(listing);
            log.info("Synced listing metadata for {} symbols", listing.size());
        } catch (Exception e) {
            log.error("Listing sync failed", e);
        }
    }
}

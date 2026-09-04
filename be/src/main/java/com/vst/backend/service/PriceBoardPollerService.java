package com.vst.backend.service;

import com.vst.backend.event.PriceBoardUpdateEvent;
import com.vst.backend.mapper.PriceQuoteMapper;
import com.vst.backend.model.PriceQuote;
import com.vst.backend.model.WatchlistEntry;
import com.vst.backend.repository.VnstockClient;
import com.vst.backend.repository.WatchlistRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Polls vnstock's price board for symbols currently on the (global) watchlist and publishes a
 * {@link PriceBoardUpdateEvent} that {@link com.vst.backend.websocket.PriceBoardWebSocketHandler}
 * broadcasts to connected clients. Scoped to the watchlist — not all ~1500 listed symbols — to
 * keep vnstock call volume low enough for a short poll interval.
 */
@Service
public class PriceBoardPollerService {

    private static final Logger log = LoggerFactory.getLogger(PriceBoardPollerService.class);

    private final VnstockClient vnstockClient;
    private final PriceQuoteMapper mapper;
    private final WatchlistRepository watchlistRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PriceBoardPollerService(
            VnstockClient vnstockClient,
            PriceQuoteMapper mapper,
            WatchlistRepository watchlistRepository,
            ApplicationEventPublisher eventPublisher) {
        this.vnstockClient = vnstockClient;
        this.mapper = mapper;
        this.watchlistRepository = watchlistRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "${price-board.poll-cron}")
    public void poll() {
        List<String> symbols = watchlistRepository.findAll().stream().map(WatchlistEntry::symbol).toList();
        if (symbols.isEmpty()) {
            return;
        }

        try {
            List<PriceQuote> quotes =
                    vnstockClient.fetchPriceBoard(symbols).stream().map(mapper::toDomain).toList();
            eventPublisher.publishEvent(new PriceBoardUpdateEvent(quotes));
        } catch (Exception e) {
            log.error("Price board poll failed", e);
        }
    }
}

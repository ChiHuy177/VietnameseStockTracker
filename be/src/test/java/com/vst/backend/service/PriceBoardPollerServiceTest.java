package com.vst.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.vst.backend.dto.vnstock.VnstockPriceBoardDto;
import com.vst.backend.event.PriceBoardUpdateEvent;
import com.vst.backend.mapper.PriceQuoteMapper;
import com.vst.backend.model.PriceQuote;
import com.vst.backend.model.WatchlistEntry;
import com.vst.backend.repository.VnstockClient;
import com.vst.backend.repository.WatchlistRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PriceBoardPollerServiceTest {

    @Mock
    private VnstockClient vnstockClient;

    @Mock
    private PriceQuoteMapper mapper;

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final WatchlistEntry watchlistEntry =
            new WatchlistEntry("VNM", "CTCP Sữa Việt Nam", "HOSE", Instant.now());

    @Test
    void doesNothingWhenWatchlistIsEmpty() {
        PriceBoardPollerService service =
                new PriceBoardPollerService(vnstockClient, mapper, watchlistRepository, eventPublisher);
        when(watchlistRepository.findAll()).thenReturn(List.of());

        service.poll();

        verifyNoInteractions(vnstockClient, eventPublisher);
    }

    @Test
    void fetchesMapsAndPublishesEventForWatchlistSymbols() {
        PriceBoardPollerService service =
                new PriceBoardPollerService(vnstockClient, mapper, watchlistRepository, eventPublisher);
        VnstockPriceBoardDto dto = new VnstockPriceBoardDto(
                "VNM", 1L, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, 1L);
        PriceQuote quote = new PriceQuote(
                "VNM", Instant.ofEpochMilli(1L), BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, 1L);
        when(watchlistRepository.findAll()).thenReturn(List.of(watchlistEntry));
        when(vnstockClient.fetchPriceBoard(List.of("VNM"))).thenReturn(List.of(dto));
        when(mapper.toDomain(dto)).thenReturn(quote);

        service.poll();

        verify(eventPublisher).publishEvent(new PriceBoardUpdateEvent(List.of(quote)));
    }

    @Test
    void doesNotPropagateWhenFetchFails() {
        PriceBoardPollerService service =
                new PriceBoardPollerService(vnstockClient, mapper, watchlistRepository, eventPublisher);
        when(watchlistRepository.findAll()).thenReturn(List.of(watchlistEntry));
        when(vnstockClient.fetchPriceBoard(anyList())).thenThrow(new RuntimeException("vnstock down"));

        service.poll();

        verify(eventPublisher, never()).publishEvent(any());
    }
}

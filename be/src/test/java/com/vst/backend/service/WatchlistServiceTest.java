package com.vst.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.WatchlistEntry;
import com.vst.backend.repository.StockRepository;
import com.vst.backend.repository.WatchlistRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private WatchlistRepository watchlistRepository;

    private final WatchlistEntry entry = new WatchlistEntry("VNM", "CTCP Sữa Việt Nam", "HOSE", Instant.now());

    @Test
    void listsAllEntries() {
        WatchlistService service = new WatchlistService(stockRepository, watchlistRepository);
        when(watchlistRepository.findAll()).thenReturn(List.of(entry));

        assertThat(service.list()).containsExactly(entry);
    }

    @Test
    void addFindOrCreatesStockThenAddsToWatchlist() {
        WatchlistService service = new WatchlistService(stockRepository, watchlistRepository);
        when(stockRepository.findOrCreateBySymbol("VNM")).thenReturn(1L);
        when(watchlistRepository.findByStockId(1L)).thenReturn(Optional.of(entry));

        WatchlistEntry result = service.add(" vnm ");

        assertThat(result).isEqualTo(entry);
        verify(watchlistRepository).add(1L);
    }

    @Test
    void removeThrowsWhenStockUnknown() {
        WatchlistService service = new WatchlistService(stockRepository, watchlistRepository);
        when(stockRepository.findBySymbol("ZZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.remove("ZZZZ")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeThrowsWhenStockNotOnWatchlist() {
        WatchlistService service = new WatchlistService(stockRepository, watchlistRepository);
        when(stockRepository.findBySymbol("VNM")).thenReturn(Optional.of(1L));
        when(watchlistRepository.remove(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.remove("VNM")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeSucceedsWhenOnWatchlist() {
        WatchlistService service = new WatchlistService(stockRepository, watchlistRepository);
        when(stockRepository.findBySymbol("VNM")).thenReturn(Optional.of(1L));
        when(watchlistRepository.remove(1L)).thenReturn(true);

        service.remove("VNM");

        verify(watchlistRepository).remove(1L);
    }
}

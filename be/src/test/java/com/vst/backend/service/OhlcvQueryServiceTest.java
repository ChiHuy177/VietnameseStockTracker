package com.vst.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.vst.backend.dto.OhlcvHistoryResponse;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.OhlcvBar;
import com.vst.backend.repository.PriceHistoryRepository;
import com.vst.backend.repository.StockRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OhlcvQueryServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    private OhlcvQueryService service;

    private final LocalDate start = LocalDate.of(2026, 8, 1);
    private final LocalDate end = LocalDate.of(2026, 9, 3);

    @Test
    void returnsBarsForKnownSymbol() {
        service = new OhlcvQueryService(stockRepository, priceHistoryRepository);
        when(stockRepository.findBySymbol("VNM")).thenReturn(Optional.of(1L));
        OhlcvBar bar = new OhlcvBar(1L, Instant.parse("2026-08-25T00:00:00Z"), "1D",
                BigDecimal.valueOf(63.1), BigDecimal.valueOf(63.8), BigDecimal.valueOf(62.5),
                BigDecimal.valueOf(62.6), 3_343_900L, "vnstock");
        when(priceHistoryRepository.findByStockIdAndRange(1L, start, end)).thenReturn(List.of(bar));

        OhlcvHistoryResponse response = service.getHistory("VNM", start, end);

        assertThat(response.symbol()).isEqualTo("VNM");
        assertThat(response.bars()).hasSize(1);
        assertThat(response.bars().get(0).close()).isEqualByComparingTo("62.6");
    }

    @Test
    void normalizesSymbolCase() {
        service = new OhlcvQueryService(stockRepository, priceHistoryRepository);
        when(stockRepository.findBySymbol("VNM")).thenReturn(Optional.of(1L));
        when(priceHistoryRepository.findByStockIdAndRange(1L, start, end)).thenReturn(List.of());

        OhlcvHistoryResponse response = service.getHistory(" vnm ", start, end);

        assertThat(response.symbol()).isEqualTo("VNM");
    }

    @Test
    void throwsNotFoundForUnknownSymbol() {
        service = new OhlcvQueryService(stockRepository, priceHistoryRepository);
        when(stockRepository.findBySymbol("ZZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getHistory("ZZZZ", start, end))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}

package com.vst.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.vst.backend.dto.vnstock.VnstockPriceBoardDto;
import com.vst.backend.mapper.PriceQuoteMapper;
import com.vst.backend.model.MarketSnapshotRow;
import com.vst.backend.model.PriceQuote;
import com.vst.backend.repository.MarketSnapshotRepository;
import com.vst.backend.repository.StockRepository;
import com.vst.backend.repository.VnstockClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketSnapshotIngestionServiceTest {

    @Mock
    private VnstockClient vnstockClient;

    @Mock
    private PriceQuoteMapper mapper;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private MarketSnapshotRepository marketSnapshotRepository;

    private static PriceQuote quote(String symbol, BigDecimal price) {
        return new PriceQuote(
                symbol, Instant.ofEpochMilli(1L), price, BigDecimal.ZERO, BigDecimal.ZERO, 1L,
                null, null, null, null, null, null, null, null, BigDecimal.valueOf(1_000));
    }

    private MarketSnapshotIngestionService service() {
        return new MarketSnapshotIngestionService(vnstockClient, mapper, stockRepository, marketSnapshotRepository);
    }

    @Test
    void doesNothingWhenNoSymbolsAreKnown() {
        when(stockRepository.findAllSymbols()).thenReturn(List.of());

        service().ingestAll();

        verifyNoInteractions(vnstockClient, marketSnapshotRepository);
    }

    @Test
    void fetchesAsOneBatchFiltersUntradedSymbolsAndUpsertsTheRest() {
        when(stockRepository.findAllSymbols()).thenReturn(List.of("VNM", "DCH"));
        // fetchPriceBoard returns 2 raw dtos; mapper is stubbed below regardless of dto identity.
        when(vnstockClient.fetchPriceBoard(List.of("VNM", "DCH"))).thenReturn(List.of(rawDto(), rawDto()));
        PriceQuote traded = quote("VNM", BigDecimal.valueOf(61_200));
        PriceQuote untraded = quote("DCH", BigDecimal.ZERO); // no trade this session
        when(mapper.toDomain(any())).thenReturn(traded, untraded);
        when(stockRepository.findIdsBySymbols(List.of("VNM"))).thenReturn(Map.of("VNM", 1L));

        service().ingestAll();

        ArgumentCaptor<List<MarketSnapshotRow>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(marketSnapshotRepository).upsertAll(rowsCaptor.capture(), any(LocalDate.class));
        assertThat(rowsCaptor.getValue()).hasSize(1);
        assertThat(rowsCaptor.getValue().get(0).stockId()).isEqualTo(1L);
    }

    @Test
    void doesNotPropagateWhenFetchFails() {
        when(stockRepository.findAllSymbols()).thenReturn(List.of("VNM"));
        when(vnstockClient.fetchPriceBoard(anyList())).thenThrow(new RuntimeException("vnstock down"));

        service().ingestAll();

        verifyNoInteractions(marketSnapshotRepository);
    }

    private static VnstockPriceBoardDto rawDto() {
        return new VnstockPriceBoardDto(
                "X", 1L, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, 1L,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }
}

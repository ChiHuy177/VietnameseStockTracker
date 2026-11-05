package com.vst.backend.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vst.backend.dto.vnstock.VnstockOhlcvDto;
import com.vst.backend.mapper.OhlcvMapper;
import com.vst.backend.model.OhlcvBar;
import com.vst.backend.repository.PriceHistoryRepository;
import com.vst.backend.repository.StockRepository;
import com.vst.backend.repository.VnstockClient;

@ExtendWith(MockitoExtension.class)
class OhlcvIngestionServiceTest {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Mock
    private VnstockClient vnstockClient;

    @Mock
    private OhlcvMapper mapper;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    private static VnstockOhlcvDto sampleDto() {
        return new VnstockOhlcvDto(
                LocalDateTime.now(),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(105),
                BigDecimal.valueOf(95),
                BigDecimal.valueOf(102),
                50000L);
    }

    private static OhlcvBar sampleBar(long stockId, String symbol) {
        return new OhlcvBar(
                stockId,
                Instant.now(),
                "1D",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(105),
                BigDecimal.valueOf(95),
                BigDecimal.valueOf(102),
                50000L,
                "vnstock");
    }

    @Test
    void incrementalSyncUsesLatestDateWhenPresent() {
        OhlcvIngestionService service = new OhlcvIngestionService(
                vnstockClient, mapper, stockRepository, priceHistoryRepository,
                "VNM", false, 7);

        when(stockRepository.findOrCreateBySymbol("VNM")).thenReturn(1L);
        LocalDate latestDate = LocalDate.of(2026, 10, 14);
        when(priceHistoryRepository.findLatestDates()).thenReturn(Map.of(1L, latestDate));

        VnstockOhlcvDto dto = sampleDto();
        LocalDate today = LocalDate.now(VN_ZONE);
        when(vnstockClient.fetchOhlcv("VNM", "2026-10-14", today.toString())).thenReturn(List.of(dto));
        when(mapper.toDomain(eq(1L), eq("VNM"), any(VnstockOhlcvDto.class))).thenReturn(sampleBar(1L, "VNM"));

        service.ingestAll();

        verify(vnstockClient).fetchOhlcv("VNM", "2026-10-14", today.toString());
        verify(priceHistoryRepository).upsertAll(anyList());
    }

    @Test
    void initialSyncUsesLookbackDaysWhenNoHistory() {
        OhlcvIngestionService service = new OhlcvIngestionService(
                vnstockClient, mapper, stockRepository, priceHistoryRepository,
                "VCB", false, 14);

        when(stockRepository.findOrCreateBySymbol("VCB")).thenReturn(2L);
        when(priceHistoryRepository.findLatestDates()).thenReturn(Map.of());

        LocalDate today = LocalDate.now(VN_ZONE);
        LocalDate expectedStart = today.minusDays(14);
        VnstockOhlcvDto dto = sampleDto();
        when(vnstockClient.fetchOhlcv("VCB", expectedStart.toString(), today.toString())).thenReturn(List.of(dto));
        when(mapper.toDomain(eq(2L), eq("VCB"), any(VnstockOhlcvDto.class))).thenReturn(sampleBar(2L, "VCB"));

        service.ingestAll();

        verify(vnstockClient).fetchOhlcv("VCB", expectedStart.toString(), today.toString());
        verify(priceHistoryRepository).upsertAll(anyList());
    }

    @Test
    void failureOnOneSymbolDoesNotStopSubsequentSymbols() {
        OhlcvIngestionService service = new OhlcvIngestionService(
                vnstockClient, mapper, stockRepository, priceHistoryRepository,
                "BAD,GOOD", false, 7);

        when(stockRepository.findOrCreateBySymbol("BAD")).thenReturn(10L);
        when(stockRepository.findOrCreateBySymbol("GOOD")).thenReturn(20L);
        when(priceHistoryRepository.findLatestDates()).thenReturn(Map.of());

        LocalDate today = LocalDate.now(VN_ZONE);
        String startStr = today.minusDays(7).toString();
        String endStr = today.toString();

        when(vnstockClient.fetchOhlcv("BAD", startStr, endStr)).thenThrow(new RuntimeException("API error"));
        when(vnstockClient.fetchOhlcv("GOOD", startStr, endStr)).thenReturn(List.of(sampleDto()));
        when(mapper.toDomain(eq(20L), eq("GOOD"), any(VnstockOhlcvDto.class))).thenReturn(sampleBar(20L, "GOOD"));

        service.ingestAll();

        verify(vnstockClient).fetchOhlcv("BAD", startStr, endStr);
        verify(vnstockClient).fetchOhlcv("GOOD", startStr, endStr);
        verify(priceHistoryRepository).upsertAll(anyList());
    }
}


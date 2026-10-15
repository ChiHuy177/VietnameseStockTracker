package com.vst.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.vst.backend.dto.IndexQuoteResponse;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.OhlcvBar;
import com.vst.backend.model.StockListing;
import com.vst.backend.repository.PriceHistoryRepository;
import com.vst.backend.repository.StockRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IndexQuoteServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    private final StockListing vnindex = new StockListing("VNINDEX", "VN-INDEX", "HOSE");
    private final StockListing vn30 = new StockListing("VN30", "VN30", "HOSE");

    private static OhlcvBar barWithClose(BigDecimal close, Instant time) {
        return new OhlcvBar(1L, time, "1D", close, close, close, close, 1_000_000L, "vnstock");
    }

    @Test
    void computesChangeAndPercentChangeFromLatestTwoBars() {
        IndexQuoteService service = new IndexQuoteService(stockRepository, priceHistoryRepository);
        when(stockRepository.findIndices()).thenReturn(List.of(vnindex));
        when(stockRepository.findBySymbol("VNINDEX")).thenReturn(Optional.of(1L));

        Instant latestTime = Instant.parse("2026-10-09T00:00:00Z");
        OhlcvBar latest = barWithClose(BigDecimal.valueOf(1735.09), latestTime);
        OhlcvBar previous = barWithClose(BigDecimal.valueOf(1700.00), Instant.parse("2026-10-08T00:00:00Z"));
        when(priceHistoryRepository.findLatestTwoBars(1L)).thenReturn(List.of(latest, previous));

        List<IndexQuoteResponse> result = service.getCurrentQuotes();

        assertThat(result).hasSize(1);
        IndexQuoteResponse quote = result.get(0);
        assertThat(quote.symbol()).isEqualTo("VNINDEX");
        assertThat(quote.value()).isEqualByComparingTo("1735.09");
        assertThat(quote.change()).isEqualByComparingTo("35.09");
        assertThat(quote.percentChange()).isEqualByComparingTo("2.06");
        assertThat(quote.time()).isEqualTo(latestTime);
    }

    @Test
    void returnsZeroChangeWhenOnlyOneBarExists() {
        IndexQuoteService service = new IndexQuoteService(stockRepository, priceHistoryRepository);
        when(stockRepository.findIndices()).thenReturn(List.of(vnindex));
        when(stockRepository.findBySymbol("VNINDEX")).thenReturn(Optional.of(1L));
        when(priceHistoryRepository.findLatestTwoBars(1L))
                .thenReturn(List.of(barWithClose(BigDecimal.valueOf(1735.09), Instant.parse("2026-10-09T00:00:00Z"))));

        IndexQuoteResponse quote = service.getCurrentQuotes().get(0);

        assertThat(quote.change()).isEqualByComparingTo("0");
        assertThat(quote.percentChange()).isEqualByComparingTo("0");
    }

    @Test
    void throwsNotFoundWhenNoIndexHasPriceHistoryYet() {
        IndexQuoteService service = new IndexQuoteService(stockRepository, priceHistoryRepository);
        when(stockRepository.findIndices()).thenReturn(List.of(vnindex));
        when(stockRepository.findBySymbol("VNINDEX")).thenReturn(Optional.of(1L));
        when(priceHistoryRepository.findLatestTwoBars(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getCurrentQuotes()).isInstanceOf(ResourceNotFoundException.class);
    }

    // Một chỉ số thiếu dữ liệu không được làm sập cả list — đây là lý do endpoint từng trả 404 trên
    // DB mới dù 3 chỉ số khác đã có bar.
    @Test
    void leavesOutIndicesWithoutHistoryInsteadOfFailingTheWholeList() {
        IndexQuoteService service = new IndexQuoteService(stockRepository, priceHistoryRepository);
        when(stockRepository.findIndices()).thenReturn(List.of(vnindex, vn30));
        when(stockRepository.findBySymbol("VNINDEX")).thenReturn(Optional.of(1L));
        when(stockRepository.findBySymbol("VN30")).thenReturn(Optional.of(2L));
        when(priceHistoryRepository.findLatestTwoBars(1L)).thenReturn(List.of());
        when(priceHistoryRepository.findLatestTwoBars(2L))
                .thenReturn(List.of(barWithClose(BigDecimal.valueOf(1800.00), Instant.parse("2026-10-09T00:00:00Z"))));

        List<IndexQuoteResponse> result = service.getCurrentQuotes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).symbol()).isEqualTo("VN30");
    }

    @Test
    void returnsEmptyListWhenNoIndexIsSeeded() {
        IndexQuoteService service = new IndexQuoteService(stockRepository, priceHistoryRepository);
        when(stockRepository.findIndices()).thenReturn(List.of());

        assertThat(service.getCurrentQuotes()).isEmpty();
    }
}

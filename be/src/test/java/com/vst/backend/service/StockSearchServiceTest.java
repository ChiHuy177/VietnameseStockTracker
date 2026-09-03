package com.vst.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.vst.backend.dto.StockSummaryResponse;
import com.vst.backend.exception.ValidationException;
import com.vst.backend.model.StockListing;
import com.vst.backend.repository.StockRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockSearchServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Test
    void returnsMatches() {
        StockSearchService service = new StockSearchService(stockRepository);
        when(stockRepository.search("vnm", 20))
                .thenReturn(List.of(new StockListing("VNM", "CTCP Sữa Việt Nam", "HOSE")));

        List<StockSummaryResponse> results = service.search("vnm");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).symbol()).isEqualTo("VNM");
        assertThat(results.get(0).exchange()).isEqualTo("HOSE");
    }

    @Test
    void rejectsBlankQuery() {
        StockSearchService service = new StockSearchService(stockRepository);

        assertThatThrownBy(() -> service.search("   ")).isInstanceOf(ValidationException.class);
    }
}

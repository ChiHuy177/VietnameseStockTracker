package com.vst.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.vst.backend.dto.SectorSnapshotResponse;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.SectorAggregate;
import com.vst.backend.repository.MarketSnapshotRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SectorAnalyticsServiceTest {

    @Mock
    private MarketSnapshotRepository marketSnapshotRepository;

    @Test
    void convertsMoneyFlowFromRawVndToBillions() {
        SectorAnalyticsService service = new SectorAnalyticsService(marketSnapshotRepository);
        LocalDate date = LocalDate.of(2026, 10, 9);
        when(marketSnapshotRepository.findLatestSnapshotDate()).thenReturn(Optional.of(date));
        when(marketSnapshotRepository.findSectorAggregates(date)).thenReturn(List.of(
                new SectorAggregate("8300", "Ngân hàng", BigDecimal.valueOf(24.456), BigDecimal.valueOf(1.2345),
                        BigDecimal.valueOf(842_300_000_000L))));

        List<SectorSnapshotResponse> result = service.getSectorSnapshots();

        assertThat(result).hasSize(1);
        SectorSnapshotResponse sector = result.get(0);
        assertThat(sector.code()).isEqualTo("8300");
        assertThat(sector.marketCapWeight()).isEqualByComparingTo("24.5");
        assertThat(sector.percentChange()).isEqualByComparingTo("1.23");
        assertThat(sector.moneyFlow()).isEqualByComparingTo("842");
    }

    @Test
    void throwsNotFoundWhenNoSnapshotHasBeenIngestedYet() {
        SectorAnalyticsService service = new SectorAnalyticsService(marketSnapshotRepository);
        when(marketSnapshotRepository.findLatestSnapshotDate()).thenReturn(Optional.empty());

        assertThatThrownBy(service::getSectorSnapshots).isInstanceOf(ResourceNotFoundException.class);
    }
}

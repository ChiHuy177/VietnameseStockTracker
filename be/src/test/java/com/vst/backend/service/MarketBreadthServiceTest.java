package com.vst.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.vst.backend.dto.MarketBreadthResponse;
import com.vst.backend.exception.ResourceNotFoundException;
import com.vst.backend.model.MarketBreadth;
import com.vst.backend.repository.MarketSnapshotRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketBreadthServiceTest {

    @Mock
    private MarketSnapshotRepository marketSnapshotRepository;

    @Test
    void returnsBreadthForTheLatestSnapshotDate() {
        MarketBreadthService service = new MarketBreadthService(marketSnapshotRepository);
        LocalDate date = LocalDate.of(2026, 10, 9);
        when(marketSnapshotRepository.findLatestSnapshotDate()).thenReturn(Optional.of(date));
        when(marketSnapshotRepository.findBreadth(date)).thenReturn(new MarketBreadth(296, 330, 213, 18, 7));

        MarketBreadthResponse response = service.getBreadth();

        assertThat(response.advancing()).isEqualTo(296);
        assertThat(response.declining()).isEqualTo(330);
        assertThat(response.unchanged()).isEqualTo(213);
        assertThat(response.ceiling()).isEqualTo(18);
        assertThat(response.floor()).isEqualTo(7);
    }

    @Test
    void throwsNotFoundWhenNoSnapshotHasBeenIngestedYet() {
        MarketBreadthService service = new MarketBreadthService(marketSnapshotRepository);
        when(marketSnapshotRepository.findLatestSnapshotDate()).thenReturn(Optional.empty());

        assertThatThrownBy(service::getBreadth).isInstanceOf(ResourceNotFoundException.class);
    }
}

package com.vst.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.vst.backend.dto.vnstock.VnstockOhlcvDto;
import com.vst.backend.model.OhlcvBar;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OhlcvMapperTest {

    private final OhlcvMapper mapper = new OhlcvMapper();

    @Test
    void convertsVnTimeToUtcInstant() {
        VnstockOhlcvDto dto = new VnstockOhlcvDto(
                LocalDateTime.of(2026, 8, 25, 7, 0, 0),
                BigDecimal.valueOf(63.1),
                BigDecimal.valueOf(63.8),
                BigDecimal.valueOf(62.5),
                BigDecimal.valueOf(62.6),
                3_343_900L);

        OhlcvBar bar = mapper.toDomain(1L, dto);

        assertThat(bar.time()).isEqualTo(Instant.parse("2026-08-25T00:00:00Z"));
    }

    @Test
    void mapsAllFieldsAndAddsDomainMetadata() {
        VnstockOhlcvDto dto = new VnstockOhlcvDto(
                LocalDateTime.of(2026, 8, 25, 7, 0, 0),
                BigDecimal.valueOf(63.1),
                BigDecimal.valueOf(63.8),
                BigDecimal.valueOf(62.5),
                BigDecimal.valueOf(62.6),
                3_343_900L);

        OhlcvBar bar = mapper.toDomain(42L, dto);

        assertThat(bar.stockId()).isEqualTo(42L);
        assertThat(bar.open()).isEqualByComparingTo("63.1");
        assertThat(bar.high()).isEqualByComparingTo("63.8");
        assertThat(bar.low()).isEqualByComparingTo("62.5");
        assertThat(bar.close()).isEqualByComparingTo("62.6");
        assertThat(bar.volume()).isEqualTo(3_343_900L);
        assertThat(bar.interval()).isEqualTo("1D");
        assertThat(bar.source()).isEqualTo("vnstock");
    }
}

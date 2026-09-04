package com.vst.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vst.backend.dto.vnstock.VnstockPriceBoardDto;
import com.vst.backend.exception.DataFetchException;
import com.vst.backend.model.PriceQuote;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PriceQuoteMapperTest {

    private final PriceQuoteMapper mapper = new PriceQuoteMapper();

    private static VnstockPriceBoardDto validDto() {
        return new VnstockPriceBoardDto(
                "vnm",
                1788424353201L,
                BigDecimal.valueOf(61_200),
                BigDecimal.valueOf(-1_100),
                BigDecimal.valueOf(-1.76565),
                3_947_400L);
    }

    @Test
    void mapsAllFieldsAndUppercasesSymbol() {
        PriceQuote quote = mapper.toDomain(validDto());

        assertThat(quote.symbol()).isEqualTo("VNM");
        assertThat(quote.time()).isEqualTo(Instant.ofEpochMilli(1788424353201L));
        assertThat(quote.price()).isEqualByComparingTo("61200");
        assertThat(quote.change()).isEqualByComparingTo("-1100");
        assertThat(quote.percentChange()).isEqualByComparingTo("-1.76565");
        assertThat(quote.volume()).isEqualTo(3_947_400L);
    }

    @Test
    void rejectsNullSymbol() {
        VnstockPriceBoardDto dto = new VnstockPriceBoardDto(
                null, 1L, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1L);

        assertThatThrownBy(() -> mapper.toDomain(dto))
                .isInstanceOf(DataFetchException.class)
                .hasMessageContaining("symbol");
    }

    @Test
    void rejectsNullTime() {
        VnstockPriceBoardDto dto = new VnstockPriceBoardDto(
                "VNM", null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1L);

        assertThatThrownBy(() -> mapper.toDomain(dto))
                .isInstanceOf(DataFetchException.class)
                .hasMessageContaining("time");
    }

    @Test
    void rejectsNullClosePrice() {
        VnstockPriceBoardDto dto = new VnstockPriceBoardDto(
                "VNM", 1L, null, BigDecimal.ONE, BigDecimal.ONE, 1L);

        assertThatThrownBy(() -> mapper.toDomain(dto))
                .isInstanceOf(DataFetchException.class)
                .hasMessageContaining("close_price");
    }

    @Test
    void rejectsNullVolume() {
        VnstockPriceBoardDto dto = new VnstockPriceBoardDto(
                "VNM", 1L, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null);

        assertThatThrownBy(() -> mapper.toDomain(dto))
                .isInstanceOf(DataFetchException.class)
                .hasMessageContaining("volume_accumulated");
    }
}

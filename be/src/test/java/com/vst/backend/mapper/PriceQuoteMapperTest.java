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

    /**
     * DTO chỉ có 6 field lõi, toàn bộ field mở rộng null — đúng những gì vnstock trả ngoài giờ giao
     * dịch hoặc với mã không ai đặt lệnh.
     */
    private static VnstockPriceBoardDto coreOnly(
            String symbol, Long time, BigDecimal close, BigDecimal change, BigDecimal percentChange, Long volume) {
        return new VnstockPriceBoardDto(
                symbol, time, close, change, percentChange, volume,
                null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null);
    }

    private static VnstockPriceBoardDto validDto() {
        return coreOnly(
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
    void mapsExtendedFieldsAndFoldsFlatBidAskIntoLevels() {
        VnstockPriceBoardDto dto = new VnstockPriceBoardDto(
                "VNM", 1L,
                BigDecimal.valueOf(61_200), BigDecimal.valueOf(-1_100), BigDecimal.valueOf(-1.76), 3_947_400L,
                BigDecimal.valueOf(62_300), BigDecimal.valueOf(66_600), BigDecimal.valueOf(58_000),
                BigDecimal.valueOf(61_100), 12_300L,
                BigDecimal.valueOf(61_000), 45_600L,
                BigDecimal.valueOf(60_900), 78_900L,
                BigDecimal.valueOf(61_300), 11_100L,
                BigDecimal.valueOf(61_400), 22_200L,
                BigDecimal.valueOf(61_500), 33_300L,
                500_000L, 420_000L, 1_234_567L,
                BigDecimal.valueOf(241_500_000_000L));

        PriceQuote quote = mapper.toDomain(dto);

        assertThat(quote.referencePrice()).isEqualByComparingTo("62300");
        assertThat(quote.ceilingPrice()).isEqualByComparingTo("66600");
        assertThat(quote.floorPrice()).isEqualByComparingTo("58000");

        assertThat(quote.bids()).hasSize(3);
        assertThat(quote.bids().get(0).price()).isEqualByComparingTo("61100");
        assertThat(quote.bids().get(0).volume()).isEqualTo(12_300L);
        assertThat(quote.bids().get(2).price()).isEqualByComparingTo("60900");

        assertThat(quote.asks()).hasSize(3);
        assertThat(quote.asks().get(0).price()).isEqualByComparingTo("61300");
        assertThat(quote.asks().get(2).volume()).isEqualTo(33_300L);

        assertThat(quote.foreignBuyVolume()).isEqualTo(500_000L);
        assertThat(quote.foreignSellVolume()).isEqualTo(420_000L);
        assertThat(quote.foreignRoom()).isEqualTo(1_234_567L);
        assertThat(quote.totalValue()).isEqualByComparingTo("241500000000");
    }

    // Khác 6 field lõi: field mở rộng thiếu thì map bình thường, không throw.
    @Test
    void leavesExtendedFieldsNullWhenVnstockOmitsThem() {
        PriceQuote quote = mapper.toDomain(validDto());

        assertThat(quote.referencePrice()).isNull();
        assertThat(quote.ceilingPrice()).isNull();
        assertThat(quote.floorPrice()).isNull();
        assertThat(quote.bids()).isNull();
        assertThat(quote.asks()).isNull();
        assertThat(quote.foreignBuyVolume()).isNull();
        assertThat(quote.foreignSellVolume()).isNull();
        assertThat(quote.foreignRoom()).isNull();
        assertThat(quote.totalValue()).isNull();
    }

    @Test
    void keepsOnlyBidAskLevelsThatHaveAPrice() {
        VnstockPriceBoardDto dto = new VnstockPriceBoardDto(
                "VNM", 1L, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, 1L,
                null, null, null,
                BigDecimal.valueOf(61_100), null,
                null, 45_600L,
                null, null,
                null, null,
                null, null,
                null, null,
                null, null, null, null);

        PriceQuote quote = mapper.toDomain(dto);

        // Mức 2 có khối lượng nhưng không có giá -> không phải một mức giá, bỏ.
        assertThat(quote.bids()).hasSize(1);
        assertThat(quote.bids().get(0).price()).isEqualByComparingTo("61100");
        // Giá có, khối lượng thiếu -> 0, không bỏ cả mức.
        assertThat(quote.bids().get(0).volume()).isZero();
        assertThat(quote.asks()).isNull();
    }

    @Test
    void rejectsNullSymbol() {
        VnstockPriceBoardDto dto = coreOnly(null, 1L, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1L);

        assertThatThrownBy(() -> mapper.toDomain(dto))
                .isInstanceOf(DataFetchException.class)
                .hasMessageContaining("symbol");
    }

    @Test
    void rejectsNullTime() {
        VnstockPriceBoardDto dto = coreOnly("VNM", null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1L);

        assertThatThrownBy(() -> mapper.toDomain(dto))
                .isInstanceOf(DataFetchException.class)
                .hasMessageContaining("time");
    }

    @Test
    void rejectsNullClosePrice() {
        VnstockPriceBoardDto dto = coreOnly("VNM", 1L, null, BigDecimal.ONE, BigDecimal.ONE, 1L);

        assertThatThrownBy(() -> mapper.toDomain(dto))
                .isInstanceOf(DataFetchException.class)
                .hasMessageContaining("close_price");
    }

    @Test
    void rejectsNullVolume() {
        VnstockPriceBoardDto dto = coreOnly("VNM", 1L, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, null);

        assertThatThrownBy(() -> mapper.toDomain(dto))
                .isInstanceOf(DataFetchException.class)
                .hasMessageContaining("volume_accumulated");
    }
}

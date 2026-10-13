package com.vst.backend.mapper;

import com.vst.backend.dto.vnstock.VnstockPriceBoardDto;
import com.vst.backend.exception.DataFetchException;
import com.vst.backend.model.PriceLevel;
import com.vst.backend.model.PriceQuote;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Anti-corruption layer: translates vnstock's price board JSON shape into our own domain model.
 *
 * <p>6 field lõi (symbol/time/giá khớp/thay đổi/%/khối lượng) là bắt buộc, thiếu thì fail ngay tại
 * đây cho gần nguồn. Các field mở rộng (trần/sàn/tham chiếu, bid/ask, khối ngoại) thì **không**:
 * vnstock không luôn trả đủ — ngoài giờ giao dịch hoặc mã không có ai đặt lệnh là null bình thường
 * — nên cứ để null, domain model đã khai optional.
 */
@Component
public class PriceQuoteMapper {

    public PriceQuote toDomain(VnstockPriceBoardDto dto) {
        requireField(dto.symbol(), "symbol");
        requireField(dto.time(), "time");
        requireField(dto.closePrice(), "close_price");
        requireField(dto.priceChange(), "price_change");
        requireField(dto.percentChange(), "percent_change");
        requireField(dto.volumeAccumulated(), "volume_accumulated");

        return new PriceQuote(
                dto.symbol().strip().toUpperCase(),
                Instant.ofEpochMilli(dto.time()),
                dto.closePrice(),
                dto.priceChange(),
                dto.percentChange(),
                dto.volumeAccumulated(),
                dto.referencePrice(),
                dto.ceilingPrice(),
                dto.floorPrice(),
                toLevels(dto.bidPrice1(), dto.bidVol1(), dto.bidPrice2(), dto.bidVol2(), dto.bidPrice3(), dto.bidVol3()),
                toLevels(dto.askPrice1(), dto.askVol1(), dto.askPrice2(), dto.askVol2(), dto.askPrice3(), dto.askVol3()),
                dto.foreignBuyVolume(),
                dto.foreignSellVolume(),
                dto.foreignRoom(),
                dto.totalValue());
    }

    /**
     * Gom 3 mức giá phẳng thành list, bỏ qua mức không có giá. Trả {@code null} (chứ không phải
     * list rỗng) khi không còn mức nào: FE phân biệt "không có bảng lệnh" bằng field vắng mặt, list
     * rỗng sẽ ra một cái bảng chỉ có header.
     */
    private static List<PriceLevel> toLevels(
            BigDecimal price1, Long volume1, BigDecimal price2, Long volume2, BigDecimal price3, Long volume3) {
        List<PriceLevel> levels = new ArrayList<>(3);
        addLevel(levels, price1, volume1);
        addLevel(levels, price2, volume2);
        addLevel(levels, price3, volume3);

        return levels.isEmpty() ? null : List.copyOf(levels);
    }

    private static void addLevel(List<PriceLevel> levels, BigDecimal price, Long volume) {
        if (price == null) {
            return;
        }

        // Giá có mà khối lượng null là trường hợp vnstock trả thiếu, không phải "0 lệnh" — nhưng một
        // mức giá không khối lượng thì hiển thị 0 vẫn đúng hơn là bỏ luôn cả mức giá đó.
        levels.add(new PriceLevel(price, volume == null ? 0L : volume));
    }

    private static void requireField(Object value, String field) {
        if (value == null) {
            throw new DataFetchException("vnstock returned null \"" + field + "\" in price board response");
        }
    }
}

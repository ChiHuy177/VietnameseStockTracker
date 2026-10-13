package com.vst.backend.dto.vnstock;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * Raw shape returned by the Python vnstock microservice's {@code GET /price-board} endpoint.
 * Deliberately mirrors the external JSON as-is — translated to a domain model in
 * {@link com.vst.backend.mapper.PriceQuoteMapper}.
 *
 * <p>Chỉ 6 field đầu là bắt buộc (mapper throw nếu thiếu). Phần còn lại vnstock không luôn trả đủ
 * — ngoài giờ giao dịch, hoặc mã không có ai đặt lệnh mua/bán, thì null là bình thường. Bid/ask để
 * phẳng đúng như JSON ({@code bid_price_1..3}), việc gom thành mảng là của mapper.
 */
public record VnstockPriceBoardDto(
        String symbol,
        Long time,
        @JsonProperty("close_price") BigDecimal closePrice,
        @JsonProperty("price_change") BigDecimal priceChange,
        @JsonProperty("percent_change") BigDecimal percentChange,
        @JsonProperty("volume_accumulated") Long volumeAccumulated,
        @JsonProperty("reference_price") BigDecimal referencePrice,
        @JsonProperty("ceiling_price") BigDecimal ceilingPrice,
        @JsonProperty("floor_price") BigDecimal floorPrice,
        @JsonProperty("bid_price_1") BigDecimal bidPrice1,
        @JsonProperty("bid_vol_1") Long bidVol1,
        @JsonProperty("bid_price_2") BigDecimal bidPrice2,
        @JsonProperty("bid_vol_2") Long bidVol2,
        @JsonProperty("bid_price_3") BigDecimal bidPrice3,
        @JsonProperty("bid_vol_3") Long bidVol3,
        @JsonProperty("ask_price_1") BigDecimal askPrice1,
        @JsonProperty("ask_vol_1") Long askVol1,
        @JsonProperty("ask_price_2") BigDecimal askPrice2,
        @JsonProperty("ask_vol_2") Long askVol2,
        @JsonProperty("ask_price_3") BigDecimal askPrice3,
        @JsonProperty("ask_vol_3") Long askVol3,
        @JsonProperty("foreign_buy_volume") Long foreignBuyVolume,
        @JsonProperty("foreign_sell_volume") Long foreignSellVolume,
        @JsonProperty("foreign_room") Long foreignRoom,
        // Chưa có UI nào dùng: đây là input cho phần "dòng tiền theo ngành" (xem CLAUDE.md), map
        // luôn ở đây để khỏi phải sửa lại tầng DTO/mapper lần nữa khi tới lúc làm.
        @JsonProperty("total_value") BigDecimal totalValue) {
}

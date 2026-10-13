package com.vst.backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * One real-time price tick for a stock, ready to broadcast over WebSocket.
 *
 * <p>6 field đầu luôn có (mapper bắt buộc). Phần còn lại optional — vnstock không luôn trả đủ, nên
 * {@code null} là trạng thái bình thường, không phải lỗi.
 *
 * <p>{@code NON_NULL} là cố ý, không phải để tiết kiệm byte: FE khai các field này là optional và
 * phân biệt "chưa có" bằng {@code undefined} (ẩn bảng mua/bán, hiện "—") — gửi {@code null} lên sẽ
 * làm FE coi như "có dữ liệu" rồi format một giá trị không tồn tại.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PriceQuote(
        String symbol,
        Instant time,
        BigDecimal price,
        BigDecimal change,
        BigDecimal percentChange,
        long volume,
        BigDecimal referencePrice,
        BigDecimal ceilingPrice,
        BigDecimal floorPrice,
        List<PriceLevel> bids,
        List<PriceLevel> asks,
        Long foreignBuyVolume,
        Long foreignSellVolume,
        Long foreignRoom,
        /** Giá trị khớp lệnh luỹ kế — chưa UI nào dùng, để dành cho phần "dòng tiền theo ngành". */
        BigDecimal totalValue) {
}

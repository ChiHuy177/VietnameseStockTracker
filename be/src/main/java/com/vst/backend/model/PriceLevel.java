package com.vst.backend.model;

import java.math.BigDecimal;

/**
 * Một mức giá trong bảng lệnh mua/bán (giá + khối lượng đặt).
 *
 * <p>vnstock trả 3 mức mua và 3 mức bán dưới dạng field phẳng ({@code bid_price_1..3} /
 * {@code bid_vol_1..3}); {@link com.vst.backend.mapper.PriceQuoteMapper} gom thành list cho khớp
 * shape {@code bids}/{@code asks} mà FE đã dựng sẵn (fe/src/models/priceQuote.ts).
 */
public record PriceLevel(BigDecimal price, long volume) {
}

package com.vst.backend.mapper;

import com.vst.backend.dto.vnstock.VnstockPriceBoardDto;
import com.vst.backend.exception.DataFetchException;
import com.vst.backend.model.PriceQuote;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Anti-corruption layer: translates vnstock's price board JSON shape into our own domain model.
 * Every field is required here, so a bad quote fails loudly and close to the source.
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
                dto.volumeAccumulated());
    }

    private static void requireField(Object value, String field) {
        if (value == null) {
            throw new DataFetchException("vnstock returned null \"" + field + "\" in price board response");
        }
    }
}

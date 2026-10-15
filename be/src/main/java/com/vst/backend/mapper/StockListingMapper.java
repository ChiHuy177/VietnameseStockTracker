package com.vst.backend.mapper;

import com.vst.backend.dto.vnstock.VnstockListingItemDto;
import com.vst.backend.exception.DataFetchException;
import com.vst.backend.model.StockListing;
import org.springframework.stereotype.Component;

@Component
public class StockListingMapper {

    /** symbol is the upsert key, so unlike name/exchange it must not be null or blank. */
    public StockListing toDomain(VnstockListingItemDto dto) {
        if (dto == null || dto.symbol() == null || dto.symbol().isBlank()) {
            throw new DataFetchException("vnstock returned a listing entry with no symbol");
        }
        return new StockListing(dto.symbol().strip().toUpperCase(), dto.organName(), dto.exchange());
    }
}

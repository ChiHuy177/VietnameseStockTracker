package com.vst.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vst.backend.dto.vnstock.VnstockListingItemDto;
import com.vst.backend.exception.DataFetchException;
import com.vst.backend.model.StockListing;
import org.junit.jupiter.api.Test;

class StockListingMapperTest {

    private final StockListingMapper mapper = new StockListingMapper();

    @Test
    void mapsAllFieldsAndUppercasesSymbol() {
        VnstockListingItemDto dto = new VnstockListingItemDto("vnm", "CTCP Sữa Việt Nam", "HOSE");

        StockListing listing = mapper.toDomain(dto);

        assertThat(listing).isEqualTo(new StockListing("VNM", "CTCP Sữa Việt Nam", "HOSE"));
    }

    @Test
    void rejectsNullSymbol() {
        VnstockListingItemDto dto = new VnstockListingItemDto(null, "CTCP Sữa Việt Nam", "HOSE");

        assertThatThrownBy(() -> mapper.toDomain(dto)).isInstanceOf(DataFetchException.class);
    }

    @Test
    void rejectsBlankSymbol() {
        VnstockListingItemDto dto = new VnstockListingItemDto("  ", "CTCP Sữa Việt Nam", "HOSE");

        assertThatThrownBy(() -> mapper.toDomain(dto)).isInstanceOf(DataFetchException.class);
    }
}

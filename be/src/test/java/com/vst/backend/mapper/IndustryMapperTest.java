package com.vst.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vst.backend.dto.vnstock.VnstockIndustryDto;
import com.vst.backend.exception.DataFetchException;
import com.vst.backend.model.StockIndustry;
import org.junit.jupiter.api.Test;

class IndustryMapperTest {

    private final IndustryMapper mapper = new IndustryMapper();

    @Test
    void mapsAllFieldsAndUppercasesSymbol() {
        VnstockIndustryDto dto = new VnstockIndustryDto("vnm", 2, "3500", "Thực phẩm và đồ uống");

        StockIndustry industry = mapper.toDomain(dto);

        assertThat(industry).isEqualTo(new StockIndustry("VNM", "3500", "Thực phẩm và đồ uống"));
    }

    @Test
    void rejectsNullSymbol() {
        VnstockIndustryDto dto = new VnstockIndustryDto(null, 2, "3500", "Thực phẩm và đồ uống");

        assertThatThrownBy(() -> mapper.toDomain(dto)).isInstanceOf(DataFetchException.class);
    }

    @Test
    void rejectsBlankIcbCode() {
        VnstockIndustryDto dto = new VnstockIndustryDto("VNM", 2, "  ", "Thực phẩm và đồ uống");

        assertThatThrownBy(() -> mapper.toDomain(dto)).isInstanceOf(DataFetchException.class);
    }
}

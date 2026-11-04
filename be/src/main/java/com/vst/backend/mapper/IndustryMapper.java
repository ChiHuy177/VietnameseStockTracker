package com.vst.backend.mapper;

import com.vst.backend.dto.vnstock.VnstockIndustryDto;
import com.vst.backend.exception.DataFetchException;
import com.vst.backend.model.StockIndustry;
import org.springframework.stereotype.Component;

/**
 * Anti-corruption layer for {@code GET /industries}. Which ICB level qualifies is a selection
 * decision made by the caller ({@link com.vst.backend.service.IndustrySyncService}) — this mapper
 * only translates a single already-selected row.
 */
@Component
public class IndustryMapper {

    public StockIndustry toDomain(VnstockIndustryDto dto) {
        if (dto == null || dto.symbol() == null || dto.symbol().isBlank()) {
            throw new DataFetchException("vnstock returned an industry entry with no symbol");
        }
        if (dto.icbCode() == null || dto.icbCode().isBlank()) {
            throw new DataFetchException("vnstock returned an industry entry with no icb_code for " + dto.symbol());
        }

        return new StockIndustry(dto.symbol().strip().toUpperCase(), dto.icbCode(), dto.icbName());
    }
}

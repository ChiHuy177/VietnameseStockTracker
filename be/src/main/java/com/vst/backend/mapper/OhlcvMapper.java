package com.vst.backend.mapper;

import com.vst.backend.dto.vnstock.VnstockOhlcvDto;
import com.vst.backend.model.OhlcvBar;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * Anti-corruption layer: translates vnstock's external JSON shape into our own domain model.
 * If vnstock changes its response shape (as its own docs already disagree with each other),
 * only this class needs to change.
 */
@Component
public class OhlcvMapper {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String DEFAULT_INTERVAL = "1D";
    private static final String SOURCE = "vnstock";

    public OhlcvBar toDomain(long stockId, VnstockOhlcvDto dto) {
        return new OhlcvBar(
                stockId,
                dto.time().atZone(VN_ZONE).toInstant(),
                DEFAULT_INTERVAL,
                dto.open(),
                dto.high(),
                dto.low(),
                dto.close(),
                dto.volume(),
                SOURCE);
    }
}

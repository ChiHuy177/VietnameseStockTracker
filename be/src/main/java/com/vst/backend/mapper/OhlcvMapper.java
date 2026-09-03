package com.vst.backend.mapper;

import com.vst.backend.dto.vnstock.VnstockOhlcvDto;
import com.vst.backend.exception.DataFetchException;
import com.vst.backend.model.OhlcvBar;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * Anti-corruption layer: translates vnstock's external JSON shape into our own domain model.
 * If vnstock changes its response shape (as its own docs already disagree with each other),
 * only this class needs to change. Also the first line of defense against malformed upstream
 * data — every field is required here, so a bad bar fails loudly and close to the source instead
 * of surfacing later as a cryptic NPE or a DB not-null constraint violation.
 */
@Component
public class OhlcvMapper {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String DEFAULT_INTERVAL = "1D";
    private static final String SOURCE = "vnstock";

    public OhlcvBar toDomain(long stockId, String symbol, VnstockOhlcvDto dto) {
        requireField(dto, symbol, "response");
        requireField(dto.time(), symbol, "time");
        requireField(dto.open(), symbol, "open");
        requireField(dto.high(), symbol, "high");
        requireField(dto.low(), symbol, "low");
        requireField(dto.close(), symbol, "close");
        requireField(dto.volume(), symbol, "volume");

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

    private static void requireField(Object value, String symbol, String field) {
        if (value == null) {
            throw new DataFetchException(
                    "vnstock returned null \"" + field + "\" for symbol " + symbol);
        }
    }
}

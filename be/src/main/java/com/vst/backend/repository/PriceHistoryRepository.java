package com.vst.backend.repository;

import com.vst.backend.model.OhlcvBar;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PriceHistoryRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO price_history_ohlcv (stock_id, time, interval, open, high, low, close, volume, source)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (stock_id, time, interval) DO UPDATE SET
                open = EXCLUDED.open,
                high = EXCLUDED.high,
                low = EXCLUDED.low,
                close = EXCLUDED.close,
                volume = EXCLUDED.volume,
                source = EXCLUDED.source,
                fetched_at = now()
            """;

    private static final String FIND_BY_RANGE_SQL = """
            SELECT stock_id, time, interval, open, high, low, close, volume, source
            FROM price_history_ohlcv
            WHERE stock_id = ? AND time >= ? AND time < ?
            ORDER BY time
            """;

    private final JdbcTemplate jdbcTemplate;

    public PriceHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Insert-or-update every bar. Safe to call repeatedly with overlapping data (idempotent). */
    public void upsertAll(List<OhlcvBar> bars) {
        jdbcTemplate.batchUpdate(UPSERT_SQL, bars, bars.size(), (ps, bar) -> {
            ps.setLong(1, bar.stockId());
            ps.setTimestamp(2, Timestamp.from(bar.time()));
            ps.setString(3, bar.interval());
            ps.setBigDecimal(4, bar.open());
            ps.setBigDecimal(5, bar.high());
            ps.setBigDecimal(6, bar.low());
            ps.setBigDecimal(7, bar.close());
            ps.setLong(8, bar.volume());
            ps.setString(9, bar.source());
        });
    }

    /** end is inclusive (whole day) — queried as [start, end+1 day) to avoid missing intraday bars. */
    public List<OhlcvBar> findByStockIdAndRange(long stockId, LocalDate start, LocalDate end) {
        Timestamp from = Timestamp.from(start.atStartOfDay(ZoneOffset.UTC).toInstant());
        Timestamp to = Timestamp.from(end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant());

        return jdbcTemplate.query(FIND_BY_RANGE_SQL, (rs, rowNum) -> new OhlcvBar(
                rs.getLong("stock_id"),
                rs.getTimestamp("time").toInstant(),
                rs.getString("interval"),
                rs.getBigDecimal("open"),
                rs.getBigDecimal("high"),
                rs.getBigDecimal("low"),
                rs.getBigDecimal("close"),
                rs.getLong("volume"),
                rs.getString("source")),
                stockId, from, to);
    }
}

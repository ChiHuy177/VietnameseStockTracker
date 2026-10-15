package com.vst.backend.repository;

import com.vst.backend.model.MarketBreadth;
import com.vst.backend.model.MarketSnapshotRow;
import com.vst.backend.model.SectorAggregate;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MarketSnapshotRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO daily_market_snapshot
                (stock_id, snapshot_date, price, change, percent_change, total_value, ceiling_price, floor_price, source)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (stock_id, snapshot_date) DO UPDATE SET
                price = EXCLUDED.price,
                change = EXCLUDED.change,
                percent_change = EXCLUDED.percent_change,
                total_value = EXCLUDED.total_value,
                ceiling_price = EXCLUDED.ceiling_price,
                floor_price = EXCLUDED.floor_price,
                source = EXCLUDED.source,
                fetched_at = now()
            """;

    private static final String FIND_LATEST_DATE_SQL = "SELECT MAX(snapshot_date) FROM daily_market_snapshot";

    private static final String FIND_BREADTH_SQL = """
            SELECT
                count(*) FILTER (WHERE percent_change > 0) AS advancing,
                count(*) FILTER (WHERE percent_change < 0) AS declining,
                count(*) FILTER (WHERE percent_change = 0) AS unchanged,
                count(*) FILTER (WHERE price >= ceiling_price) AS ceiling,
                count(*) FILTER (WHERE price <= floor_price) AS floor
            FROM daily_market_snapshot
            WHERE snapshot_date = ?
            """;

    private static final String FIND_SECTOR_AGGREGATES_SQL = """
            WITH sector_turnover AS (
                SELECT
                    s.icb_code AS code,
                    s.icb_name AS name,
                    SUM(m.total_value) AS turnover,
                    SUM(m.total_value * m.percent_change) / NULLIF(SUM(m.total_value), 0) AS percent_change,
                    SUM(m.total_value * sign(m.percent_change)) AS money_flow
                FROM daily_market_snapshot m
                JOIN stocks s ON s.id = m.stock_id
                WHERE m.snapshot_date = ? AND s.icb_code IS NOT NULL
                GROUP BY s.icb_code, s.icb_name
            )
            SELECT code, name, percent_change, money_flow,
                   turnover / NULLIF(SUM(turnover) OVER (), 0) * 100 AS market_cap_weight
            FROM sector_turnover
            ORDER BY market_cap_weight DESC
            """;

    private final JdbcTemplate jdbcTemplate;

    public MarketSnapshotRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Insert-or-update every row for 1 snapshot date. Safe to re-run (idempotent). */
    public void upsertAll(List<MarketSnapshotRow> rows, LocalDate date) {
        jdbcTemplate.batchUpdate(UPSERT_SQL, rows, rows.size(), (ps, row) -> {
            ps.setLong(1, row.stockId());
            ps.setDate(2, Date.valueOf(date));
            ps.setBigDecimal(3, row.price());
            ps.setBigDecimal(4, row.change());
            ps.setBigDecimal(5, row.percentChange());
            ps.setBigDecimal(6, row.totalValue());
            ps.setBigDecimal(7, row.ceilingPrice());
            ps.setBigDecimal(8, row.floorPrice());
            ps.setString(9, "vnstock");
        });
    }

    /** Empty when the ingestion job hasn't run successfully yet. */
    public Optional<LocalDate> findLatestSnapshotDate() {
        Date latest = jdbcTemplate.queryForObject(FIND_LATEST_DATE_SQL, Date.class);
        return Optional.ofNullable(latest).map(Date::toLocalDate);
    }

    public MarketBreadth findBreadth(LocalDate date) {
        return jdbcTemplate.queryForObject(FIND_BREADTH_SQL, (rs, rowNum) -> new MarketBreadth(
                rs.getInt("advancing"),
                rs.getInt("declining"),
                rs.getInt("unchanged"),
                rs.getInt("ceiling"),
                rs.getInt("floor")),
                Date.valueOf(date));
    }

    public List<SectorAggregate> findSectorAggregates(LocalDate date) {
        return jdbcTemplate.query(FIND_SECTOR_AGGREGATES_SQL, (rs, rowNum) -> new SectorAggregate(
                rs.getString("code"),
                rs.getString("name"),
                rs.getBigDecimal("market_cap_weight"),
                rs.getBigDecimal("percent_change"),
                rs.getBigDecimal("money_flow")),
                Date.valueOf(date));
    }
}

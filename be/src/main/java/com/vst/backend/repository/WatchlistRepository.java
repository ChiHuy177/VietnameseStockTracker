package com.vst.backend.repository;

import com.vst.backend.model.WatchlistEntry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** A single, global watchlist — this project has no user/auth concept yet. */
@Repository
public class WatchlistRepository {

    private static final String ADD_SQL = """
            INSERT INTO watchlist_items (stock_id) VALUES (?)
            ON CONFLICT (stock_id) DO NOTHING
            """;

    private static final String REMOVE_SQL = "DELETE FROM watchlist_items WHERE stock_id = ?";

    private static final String FIND_ALL_SQL = """
            SELECT s.symbol, s.name, s.exchange, w.added_at
            FROM watchlist_items w
            JOIN stocks s ON s.id = w.stock_id
            ORDER BY w.added_at DESC
            """;

    private static final String FIND_BY_STOCK_ID_SQL = """
            SELECT s.symbol, s.name, s.exchange, w.added_at
            FROM watchlist_items w
            JOIN stocks s ON s.id = w.stock_id
            WHERE w.stock_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public WatchlistRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Idempotent: adding a stock already on the watchlist is a no-op. */
    public void add(long stockId) {
        jdbcTemplate.update(ADD_SQL, stockId);
    }

    /** Returns whether the stock was actually on the watchlist (and got removed). */
    public boolean remove(long stockId) {
        return jdbcTemplate.update(REMOVE_SQL, stockId) > 0;
    }

    public List<WatchlistEntry> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, WatchlistRepository::mapRow);
    }

    public Optional<WatchlistEntry> findByStockId(long stockId) {
        return jdbcTemplate.query(FIND_BY_STOCK_ID_SQL, WatchlistRepository::mapRow, stockId)
                .stream()
                .findFirst();
    }

    private static WatchlistEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new WatchlistEntry(
                rs.getString("symbol"),
                rs.getString("name"),
                rs.getString("exchange"),
                rs.getTimestamp("added_at").toInstant());
    }
}

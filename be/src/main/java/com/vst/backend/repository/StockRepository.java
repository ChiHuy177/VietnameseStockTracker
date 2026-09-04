package com.vst.backend.repository;

import com.vst.backend.model.StockListing;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StockRepository {

    private static final String FIND_OR_CREATE_SQL = """
            INSERT INTO stocks (symbol) VALUES (?)
            ON CONFLICT (symbol) DO UPDATE SET symbol = EXCLUDED.symbol
            RETURNING id
            """;

    private static final String FIND_BY_SYMBOL_SQL = "SELECT id FROM stocks WHERE symbol = ?";

    private static final String UPSERT_LISTING_SQL = """
            INSERT INTO stocks (symbol, name, exchange) VALUES (?, ?, ?)
            ON CONFLICT (symbol) DO UPDATE SET name = EXCLUDED.name, exchange = EXCLUDED.exchange
            """;

    private static final String SEARCH_SQL = """
            SELECT symbol, name, exchange FROM stocks
            WHERE symbol ILIKE ? OR name ILIKE ?
            ORDER BY symbol
            LIMIT ?
            """;

    private static final String FIND_ALL_SYMBOLS_SQL = "SELECT symbol FROM stocks ORDER BY symbol";

    private final JdbcTemplate jdbcTemplate;

    public StockRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Returns the stock's id, inserting a new row if the symbol isn't known yet. Used by ingestion. */
    public long findOrCreateBySymbol(String symbol) {
        return jdbcTemplate.queryForObject(FIND_OR_CREATE_SQL, Long.class, symbol);
    }

    /** Read-only lookup — does NOT create a row. Used by read endpoints (a GET must not have side effects). */
    public Optional<Long> findBySymbol(String symbol) {
        return jdbcTemplate.query(FIND_BY_SYMBOL_SQL, (rs, rowNum) -> rs.getLong("id"), symbol)
                .stream()
                .findFirst();
    }

    /** Idempotent batch upsert of company name/exchange metadata. Used by the listing sync job. */
    public void upsertListing(List<StockListing> listing) {
        jdbcTemplate.batchUpdate(UPSERT_LISTING_SQL, listing, listing.size(),
                (ps, item) -> {
                    ps.setString(1, item.symbol());
                    ps.setString(2, item.name());
                    ps.setString(3, item.exchange());
                });
    }

    /** Case-insensitive substring match on symbol or company name. Used by the search endpoint. */
    public List<StockListing> search(String query, int limit) {
        String pattern = "%" + query + "%";
        return jdbcTemplate.query(SEARCH_SQL,
                (rs, rowNum) -> new StockListing(rs.getString("symbol"), rs.getString("name"), rs.getString("exchange")),
                pattern, pattern, limit);
    }

    /** Every symbol currently known (populated by the listing sync job). Used by full-catalog ingestion. */
    public List<String> findAllSymbols() {
        return jdbcTemplate.query(FIND_ALL_SYMBOLS_SQL, (rs, rowNum) -> rs.getString("symbol"));
    }
}

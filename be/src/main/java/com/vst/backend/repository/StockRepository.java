package com.vst.backend.repository;

import com.vst.backend.model.StockIndustry;
import com.vst.backend.model.StockListing;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.ConnectionCallback;
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

    private static final String FIND_ALL_SYMBOLS_SQL = "SELECT symbol FROM stocks WHERE kind = 'STOCK' ORDER BY symbol";

    private static final String FIND_INDEX_SYMBOLS_SQL = "SELECT symbol FROM stocks WHERE kind = 'INDEX' ORDER BY symbol";

    private static final String FIND_INDICES_SQL =
            "SELECT symbol, name, exchange FROM stocks WHERE kind = 'INDEX' ORDER BY symbol";

    private static final String UPSERT_INDUSTRY_SQL =
            "UPDATE stocks SET icb_code = ?, icb_name = ? WHERE symbol = ?";

    private static final String FIND_IDS_BY_SYMBOLS_SQL = "SELECT id, symbol FROM stocks WHERE symbol = ANY(?)";

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

    /** Every tradeable stock symbol (excludes indices). Used by full-catalog ingestion. */
    public List<String> findAllSymbols() {
        return jdbcTemplate.query(FIND_ALL_SYMBOLS_SQL, (rs, rowNum) -> rs.getString("symbol"));
    }

    /** The 4 market index symbols (VNINDEX/VN30/HNX-INDEX/UPCOM-INDEX). Used by index ingestion. */
    public List<String> findIndexSymbols() {
        return jdbcTemplate.query(FIND_INDEX_SYMBOLS_SQL, (rs, rowNum) -> rs.getString("symbol"));
    }

    /** The 4 market indices with their display name/exchange. Used by the current-quote endpoint. */
    public List<StockListing> findIndices() {
        return jdbcTemplate.query(FIND_INDICES_SQL,
                (rs, rowNum) -> new StockListing(rs.getString("symbol"), rs.getString("name"), rs.getString("exchange")));
    }

    /**
     * Update-only: a symbol only gets a sector once it's already known (via listing sync). Used
     * by the ICB sync job.
     */
    public void upsertIndustries(List<StockIndustry> industries) {
        jdbcTemplate.batchUpdate(UPSERT_INDUSTRY_SQL, industries, industries.size(),
                (ps, item) -> {
                    ps.setString(1, item.icbCode());
                    ps.setString(2, item.icbName());
                    ps.setString(3, item.symbol());
                });
    }

    /**
     * Resolves ids for a batch of symbols in 1 round trip — used by the daily market snapshot job,
     * which gets all its prices back from a single {@code price_board} call and would otherwise need
     * ~1500 serial {@link #findOrCreateBySymbol} lookups to match them back to stock ids.
     */
    public Map<String, Long> findIdsBySymbols(List<String> symbols) {
        return jdbcTemplate.execute((ConnectionCallback<Map<String, Long>>) connection -> {
            Array symbolArray = connection.createArrayOf("varchar", symbols.toArray());
            try (PreparedStatement ps = connection.prepareStatement(FIND_IDS_BY_SYMBOLS_SQL)) {
                ps.setArray(1, symbolArray);
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Long> idsBySymbol = new HashMap<>();
                    while (rs.next()) {
                        idsBySymbol.put(rs.getString("symbol"), rs.getLong("id"));
                    }
                    return idsBySymbol;
                }
            }
        });
    }
}

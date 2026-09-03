package com.vst.backend.repository;

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
}

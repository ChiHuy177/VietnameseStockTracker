-- ICB industry classification, synced from vnstock's VCI source (default KBS source used by
-- StockListingSyncService doesn't support ICB — see IndustrySyncService). Nullable: indices and
-- any stock the sync hasn't covered yet simply have no sector, not an error state.
ALTER TABLE stocks
    ADD COLUMN icb_code VARCHAR(20),
    ADD COLUMN icb_name VARCHAR(100);

-- One row per stock per trading day, from a single daily `price_board` batch call (not the 3s
-- real-time poll, which stays scoped to the watchlist) — backs market breadth + sector
-- treemap/money-flow. Plain table, not a hypertable: ~1500 rows/day is nowhere near the volume
-- price_history_ohlcv/price_snapshots are built for.
CREATE TABLE daily_market_snapshot (
    stock_id       BIGINT NOT NULL REFERENCES stocks (id),
    snapshot_date  DATE NOT NULL,
    price          NUMERIC(12, 2) NOT NULL,
    change         NUMERIC(12, 2) NOT NULL,
    percent_change NUMERIC(6, 2) NOT NULL,
    total_value    NUMERIC(18, 2),
    ceiling_price  NUMERIC(12, 2),
    floor_price    NUMERIC(12, 2),
    source         VARCHAR(20) NOT NULL,
    fetched_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (stock_id, snapshot_date)
);

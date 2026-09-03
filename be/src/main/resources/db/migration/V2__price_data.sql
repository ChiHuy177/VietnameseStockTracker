-- Phase 1: price data ingestion.
-- price_history_ohlcv: 1 row per (stock, time, interval) — settled historical bars for charting.
-- price_snapshots: 1 row per (stock, snapshot_at) — high-frequency real-time quotes for Phase 3.
-- Both are TimescaleDB hypertables so retention/compression policies can differ per table later.

CREATE TABLE price_history_ohlcv (
    stock_id   BIGINT NOT NULL REFERENCES stocks (id),
    time       TIMESTAMPTZ NOT NULL,
    interval   VARCHAR(5) NOT NULL DEFAULT '1D',
    open       NUMERIC(12, 2) NOT NULL,
    high       NUMERIC(12, 2) NOT NULL,
    low        NUMERIC(12, 2) NOT NULL,
    close      NUMERIC(12, 2) NOT NULL,
    volume     BIGINT NOT NULL,
    source     VARCHAR(20) NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (stock_id, time, interval)
);

SELECT create_hypertable('price_history_ohlcv', 'time');

CREATE TABLE price_snapshots (
    stock_id       BIGINT NOT NULL REFERENCES stocks (id),
    snapshot_at    TIMESTAMPTZ NOT NULL,
    price          NUMERIC(12, 2) NOT NULL,
    change_amount  NUMERIC(12, 2),
    change_percent NUMERIC(6, 2),
    volume         BIGINT,
    source         VARCHAR(20) NOT NULL,
    PRIMARY KEY (stock_id, snapshot_at)
);

SELECT create_hypertable('price_snapshots', 'snapshot_at');

-- Phase 0: proves the app <-> Flyway <-> TimescaleDB chain works end to end.
-- Real business schema (OHLCV price history, hypertables, indicators) comes in Phase 1.
CREATE TABLE stocks (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

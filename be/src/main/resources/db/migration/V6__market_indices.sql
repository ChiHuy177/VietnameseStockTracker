-- Market indices (VNINDEX/VN30/HNX-INDEX/UPCOM-INDEX) reuse `stocks` + `price_history_ohlcv`
-- instead of a parallel table: vnstock's index OHLCV call returns the exact same shape
-- (time/open/high/low/close/volume) as the equity OHLCV call already ingested here. `kind`
-- distinguishes the two so full-catalog stock ingestion never tries to fetch an index as if it
-- were a tradeable symbol (and vice versa).
ALTER TABLE stocks
    ADD COLUMN kind VARCHAR(10) NOT NULL DEFAULT 'STOCK' CHECK (kind IN ('STOCK', 'INDEX'));

INSERT INTO stocks (symbol, name, exchange, kind) VALUES
    ('VNINDEX', 'VN-INDEX', 'HOSE', 'INDEX'),
    ('VN30', 'VN30', 'HOSE', 'INDEX'),
    ('HNXINDEX', 'HNX-INDEX', 'HNX', 'INDEX'),
    ('UPCOMINDEX', 'UPCOM-INDEX', 'UPCOM', 'INDEX')
ON CONFLICT (symbol) DO UPDATE SET kind = EXCLUDED.kind, name = EXCLUDED.name;

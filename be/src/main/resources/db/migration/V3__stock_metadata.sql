-- Phase 2 step 3: company name + exchange, populated by the listing sync job, used for search.
-- Nullable because rows created by OHLCV ingestion (findOrCreateBySymbol) predate this metadata.
ALTER TABLE stocks
    ADD COLUMN name VARCHAR(255),
    ADD COLUMN exchange VARCHAR(10);

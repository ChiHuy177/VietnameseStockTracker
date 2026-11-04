-- Phase 2 step 4: a single global watchlist (no user/auth concept exists yet in this project).
CREATE TABLE watchlist_items (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL REFERENCES stocks(id),
    added_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (stock_id)
);

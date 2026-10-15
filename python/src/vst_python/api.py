"""HTTP API cho Phase 1: Spring backend gọi sang endpoint này để lấy dữ liệu giá.

Chạy: uvicorn vst_python.api:app --reload
"""

import sys

# Trên Windows, khi stdout không gắn với console thật (chạy nền qua uvicorn, output bị
# redirect...), Python có thể fallback về codepage cp1252 ("charmap") thay vì UTF-8, khiến
# vnstock/vnai crash khi in ký tự Unicode. Ép UTF-8 ngay từ đầu để tránh phụ thuộc vào cách
# service được khởi chạy.
if sys.stdout.encoding is None or sys.stdout.encoding.lower() != "utf-8":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

import pandas as pd
from fastapi import FastAPI, HTTPException

from vst_python.fetcher import (
    fetch_index_ohlcv,
    fetch_industries,
    fetch_listing,
    fetch_ohlcv,
    fetch_price_board,
)

app = FastAPI(title="vst-python")


def _to_records(df: pd.DataFrame) -> list[dict]:
    """DataFrame -> JSON-safe records. NaN isn't valid JSON, and FastAPI's encoder crashes on it
    ("Out of range float values are not JSON compliant: nan") — only surfaces on columns vnstock
    leaves empty for some rows (e.g. `price_board` for a symbol that didn't trade), so a handful of
    watchlist symbols can go a long time without hitting it before a full-catalog batch does.

    `df.where(pd.notnull(df), None)` alone does NOT fix this: a float64 column can't hold a Python
    `None`, so numpy silently casts it straight back to NaN unless the frame is cast to `object`
    dtype first (verified manually — without `.astype(object)`, the NaNs survive untouched).
    """
    return df.astype(object).where(pd.notnull(df), None).to_dict(orient="records")


@app.get("/ohlcv")
def get_ohlcv(symbol: str, start: str, end: str):
    try:
        df = fetch_ohlcv(symbol, start, end)
    except (Exception, SystemExit) as exc:
        # vnai calls sys.exit() when its own rate limit is hit. SystemExit doesn't subclass
        # Exception, so it has to be caught explicitly or it crashes the request uncaught.
        raise HTTPException(status_code=502, detail=f"vnstock fetch failed: {exc}") from exc

    return _to_records(df)


@app.get("/index-ohlcv")
def get_index_ohlcv(symbol: str, start: str, end: str):
    try:
        df = fetch_index_ohlcv(symbol, start, end)
    except (Exception, SystemExit) as exc:
        raise HTTPException(status_code=502, detail=f"vnstock fetch failed: {exc}") from exc

    return _to_records(df)


@app.get("/listing")
def get_listing():
    try:
        df = fetch_listing()
    except (Exception, SystemExit) as exc:
        raise HTTPException(status_code=502, detail=f"vnstock fetch failed: {exc}") from exc

    return _to_records(df)


@app.get("/price-board")
def get_price_board(symbols: str):
    symbol_list = [s.strip() for s in symbols.split(",") if s.strip()]
    try:
        df = fetch_price_board(symbol_list)
    except (Exception, SystemExit) as exc:
        raise HTTPException(status_code=502, detail=f"vnstock fetch failed: {exc}") from exc

    return _to_records(df)


@app.get("/industries")
def get_industries():
    try:
        df = fetch_industries()
    except (Exception, SystemExit) as exc:
        raise HTTPException(status_code=502, detail=f"vnstock fetch failed: {exc}") from exc

    return _to_records(df)

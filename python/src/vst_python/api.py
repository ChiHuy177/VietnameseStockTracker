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

from fastapi import FastAPI, HTTPException

from vst_python.fetcher import fetch_listing, fetch_ohlcv

app = FastAPI(title="vst-python")


@app.get("/ohlcv")
def get_ohlcv(symbol: str, start: str, end: str):
    try:
        df = fetch_ohlcv(symbol, start, end)
    except (Exception, SystemExit) as exc:
        # vnai calls sys.exit() when its own rate limit is hit. SystemExit doesn't subclass
        # Exception, so it has to be caught explicitly or it crashes the request uncaught.
        raise HTTPException(status_code=502, detail=f"vnstock fetch failed: {exc}") from exc

    return df.to_dict(orient="records")


@app.get("/listing")
def get_listing():
    try:
        df = fetch_listing()
    except (Exception, SystemExit) as exc:
        raise HTTPException(status_code=502, detail=f"vnstock fetch failed: {exc}") from exc

    return df.to_dict(orient="records")

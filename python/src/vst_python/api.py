"""HTTP API cho Phase 1: Spring backend gọi sang endpoint này để lấy dữ liệu giá.

Chạy: uvicorn vst_python.api:app --reload
"""

from fastapi import FastAPI, HTTPException

from vst_python.fetcher import fetch_ohlcv

app = FastAPI(title="vst-python")


@app.get("/ohlcv")
def get_ohlcv(symbol: str, start: str, end: str):
    try:
        df = fetch_ohlcv(symbol, start, end)
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"vnstock fetch failed: {exc}") from exc

    return df.to_dict(orient="records")

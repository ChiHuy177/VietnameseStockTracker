"""Fetch stock price data from vnstock.

Phase 1 scaffold: wraps vnstock's Market API behind a small function so the
rest of the codebase (and later, the mapper/anti-corruption layer) doesn't
depend on vnstock's class structure directly.
"""

from vnstock import Market

_market = Market()


def fetch_ohlcv(symbol: str, start: str, end: str):
    """Fetch daily OHLCV history for one symbol.

    symbol: mã cổ phiếu, ví dụ "VNM"
    start, end: định dạng "YYYY-MM-DD"

    Trả về pandas.DataFrame với các cột: time, open, high, low, close, volume.
    """
    return _market.equity(symbol).ohlcv(start=start, end=end)


if __name__ == "__main__":
    df = fetch_ohlcv("VNM", "2026-08-01", "2026-09-03")
    print(df)

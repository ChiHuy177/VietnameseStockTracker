import type { OhlcvBar } from '@/models/ohlcvBar';
import type { CandlestickData, HistogramData, UTCTimestamp } from 'lightweight-charts';

const UP_COLOR = '#16a34a';
const DOWN_COLOR = '#dc2626';

function toUtcTimestamp(isoTime: string): UTCTimestamp {
  return Math.floor(new Date(isoTime).getTime() / 1000) as UTCTimestamp;
}

export function toCandlestickData(bars: OhlcvBar[]): CandlestickData[] {
  return bars.map((bar) => ({
    time: toUtcTimestamp(bar.time),
    open: bar.open,
    high: bar.high,
    low: bar.low,
    close: bar.close,
  }));
}

export function toVolumeData(bars: OhlcvBar[]): HistogramData[] {
  return bars.map((bar) => ({
    time: toUtcTimestamp(bar.time),
    value: bar.volume,
    color: bar.close >= bar.open ? UP_COLOR : DOWN_COLOR,
  }));
}

import type { OhlcvBar } from '@/models/ohlcvBar';
import type { CandlestickData, HistogramData, UTCTimestamp } from 'lightweight-charts';

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

export function toVolumeData(bars: OhlcvBar[], upColor: string, downColor: string): HistogramData[] {
  return bars.map((bar) => ({
    time: toUtcTimestamp(bar.time),
    value: bar.volume,
    color: bar.close >= bar.open ? upColor : downColor,
  }));
}

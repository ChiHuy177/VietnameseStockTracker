import { describe, expect, it } from 'vitest';
import { toCandlestickData, toVolumeData } from './ohlcvChartData';

const bars = [
  { time: '2026-09-01T00:00:00Z', open: 100, high: 110, low: 95, close: 105, volume: 1000 },
  { time: '2026-09-02T00:00:00Z', open: 105, high: 108, low: 100, close: 102, volume: 800 },
];

describe('toCandlestickData', () => {
  it('maps OHLC fields and converts time to a unix timestamp in seconds', () => {
    const result = toCandlestickData(bars);

    expect(result).toEqual([
      { time: Math.floor(new Date('2026-09-01T00:00:00Z').getTime() / 1000), open: 100, high: 110, low: 95, close: 105 },
      { time: Math.floor(new Date('2026-09-02T00:00:00Z').getTime() / 1000), open: 105, high: 108, low: 100, close: 102 },
    ]);
  });
});

describe('toVolumeData', () => {
  it('colors up bars (close >= open) green and down bars red', () => {
    const result = toVolumeData(bars);

    expect(result[0]).toMatchObject({ value: 1000, color: '#16a34a' });
    expect(result[1]).toMatchObject({ value: 800, color: '#dc2626' });
  });
});

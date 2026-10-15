import { describe, expect, it } from 'vitest';
import { priceBoardReducer, quotesReceived } from './priceBoardSlice';

describe('priceBoardReducer', () => {
  it('starts with no quotes', () => {
    const state = priceBoardReducer(undefined, { type: '@@INIT' });

    expect(state.quotesBySymbol).toEqual({});
  });

  it('stores quotes keyed by symbol', () => {
    const state = priceBoardReducer(
      undefined,
      quotesReceived([
        { symbol: 'VNM', time: '2026-09-03T00:00:00Z', price: 61900, change: 700, percentChange: 1.14, volume: 100 },
      ]),
    );

    expect(state.quotesBySymbol.VNM?.price).toBe(61900);
  });

  it('overwrites the previous quote for the same symbol', () => {
    const afterFirst = priceBoardReducer(
      undefined,
      quotesReceived([{ symbol: 'VNM', time: 't1', price: 100, change: 0, percentChange: 0, volume: 1 }]),
    );
    const afterSecond = priceBoardReducer(
      afterFirst,
      quotesReceived([{ symbol: 'VNM', time: 't2', price: 200, change: 0, percentChange: 0, volume: 1 }]),
    );

    expect(afterSecond.quotesBySymbol.VNM?.price).toBe(200);
  });
});

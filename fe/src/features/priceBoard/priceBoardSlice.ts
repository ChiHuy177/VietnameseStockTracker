import type { PriceQuote } from '@/models/priceQuote';
import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface PriceBoardState {
  quotesBySymbol: Record<string, PriceQuote>;
}

const initialState: PriceBoardState = {
  quotesBySymbol: {},
};

const priceBoardSlice = createSlice({
  name: 'priceBoard',
  initialState,
  reducers: {
    quotesReceived(state, action: PayloadAction<PriceQuote[]>) {
      for (const quote of action.payload) {
        state.quotesBySymbol[quote.symbol] = quote;
      }
    },
  },
});

export const { quotesReceived } = priceBoardSlice.actions;
export const priceBoardReducer = priceBoardSlice.reducer;

export function selectQuote(symbol: string) {
  return (state: { priceBoard: PriceBoardState }) => state.priceBoard.quotesBySymbol[symbol];
}

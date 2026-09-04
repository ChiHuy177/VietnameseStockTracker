import { priceBoardReducer } from '@/features/priceBoard/priceBoardSlice';
import * as stockService from '@/services/stockService';
import { configureStore } from '@reduxjs/toolkit';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { StockDetailPage } from './StockDetailPage';

vi.mock('@/services/stockService');

function renderDetailPage(symbol: string) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const store = configureStore({ reducer: { priceBoard: priceBoardReducer } });

  return render(
    <QueryClientProvider client={queryClient}>
      <Provider store={store}>
        <MemoryRouter initialEntries={[`/stocks/${symbol}`]}>
          <Routes>
            <Route path="/stocks/:symbol" element={<StockDetailPage />} />
          </Routes>
        </MemoryRouter>
      </Provider>
    </QueryClientProvider>,
  );
}

describe('StockDetailPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('renders the symbol heading and chart once history loads', async () => {
    vi.mocked(stockService.getOhlcvHistory).mockResolvedValue({
      symbol: 'VNM',
      bars: [{ time: '2026-09-01T00:00:00Z', open: 100, high: 110, low: 95, close: 105, volume: 1000 }],
    });

    renderDetailPage('VNM');

    expect(await screen.findByRole('heading', { name: 'VNM' })).toBeInTheDocument();
  });

  it('shows an empty-history message when there are no bars', async () => {
    vi.mocked(stockService.getOhlcvHistory).mockResolvedValue({ symbol: 'VNM', bars: [] });

    renderDetailPage('VNM');

    expect(await screen.findByText(/chưa có dữ liệu lịch sử/i)).toBeInTheDocument();
  });
});

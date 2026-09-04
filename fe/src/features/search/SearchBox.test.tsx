import { renderWithProviders } from '@/test/renderWithProviders';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as stockService from '@/services/stockService';
import * as watchlistService from '@/services/watchlistService';
import { SearchBox } from './SearchBox';

vi.mock('@/services/stockService');
vi.mock('@/services/watchlistService');

describe('SearchBox', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('searches and shows results as the user types', async () => {
    vi.mocked(stockService.searchStocks).mockResolvedValue([
      { symbol: 'VNM', name: 'CTCP Sữa Việt Nam', exchange: 'HOSE' },
    ]);

    renderWithProviders(<SearchBox />);
    await userEvent.type(screen.getByPlaceholderText(/tìm mã/i), 'vnm');

    expect(await screen.findByText(/VNM/)).toBeInTheDocument();
    await waitFor(() => expect(stockService.searchStocks).toHaveBeenCalledWith('vnm'));
  });

  it('adds a result to the watchlist on click', async () => {
    vi.mocked(stockService.searchStocks).mockResolvedValue([
      { symbol: 'VNM', name: 'CTCP Sữa Việt Nam', exchange: 'HOSE' },
    ]);
    vi.mocked(watchlistService.addToWatchlist).mockResolvedValue({
      symbol: 'VNM',
      name: 'CTCP Sữa Việt Nam',
      exchange: 'HOSE',
      addedAt: '2026-09-03T00:00:00Z',
    });

    renderWithProviders(<SearchBox />);
    await userEvent.type(screen.getByPlaceholderText(/tìm mã/i), 'vnm');
    await screen.findByText(/VNM/);

    await userEvent.click(screen.getByRole('button', { name: /watchlist/i }));

    await waitFor(() =>
      expect(watchlistService.addToWatchlist).toHaveBeenCalledWith('VNM', expect.anything()),
    );
  });
});

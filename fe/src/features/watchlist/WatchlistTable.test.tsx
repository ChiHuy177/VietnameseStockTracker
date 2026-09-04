import { renderWithProviders } from '@/test/renderWithProviders';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import * as watchlistService from '@/services/watchlistService';
import { WatchlistTable } from './WatchlistTable';

vi.mock('@/services/watchlistService');

describe('WatchlistTable', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('shows an empty state when the watchlist has no items', async () => {
    vi.mocked(watchlistService.getWatchlist).mockResolvedValue([]);

    renderWithProviders(<WatchlistTable />);

    expect(await screen.findByText(/watchlist trống/i)).toBeInTheDocument();
  });

  it('lists watchlist items and removes one on click', async () => {
    vi.mocked(watchlistService.getWatchlist).mockResolvedValue([
      { symbol: 'VNM', name: 'CTCP Sữa Việt Nam', exchange: 'HOSE', addedAt: '2026-09-03T00:00:00Z' },
    ]);
    vi.mocked(watchlistService.removeFromWatchlist).mockResolvedValue(undefined);

    renderWithProviders(<WatchlistTable />);

    expect(await screen.findByText('VNM')).toBeInTheDocument();

    await userEvent.click(screen.getByRole('button', { name: /xóa/i }));

    await waitFor(() =>
      expect(watchlistService.removeFromWatchlist).toHaveBeenCalledWith('VNM', expect.anything()),
    );
  });

  it('shows the live price when a quote is available in the store', async () => {
    vi.mocked(watchlistService.getWatchlist).mockResolvedValue([
      { symbol: 'VNM', name: 'CTCP Sữa Việt Nam', exchange: 'HOSE', addedAt: '2026-09-03T00:00:00Z' },
    ]);

    renderWithProviders(<WatchlistTable />, {
      preloadedState: {
        priceBoard: {
          quotesBySymbol: {
            VNM: { symbol: 'VNM', time: '2026-09-03T00:00:00Z', price: 61900, change: 700, percentChange: 1.14, volume: 1 },
          },
        },
      },
    });

    expect(await screen.findByText(/\+1\.14%/)).toBeInTheDocument();
  });
});

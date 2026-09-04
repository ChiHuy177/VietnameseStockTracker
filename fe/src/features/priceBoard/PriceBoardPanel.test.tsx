import { renderWithProviders } from '@/test/renderWithProviders';
import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { PriceBoardPanel } from './PriceBoardPanel';

describe('PriceBoardPanel', () => {
  it('shows a placeholder when there is no quote yet', () => {
    renderWithProviders(<PriceBoardPanel symbol="VNM" />);

    expect(screen.getByText(/chưa có dữ liệu real-time/i)).toBeInTheDocument();
  });

  it('shows price and change without a bid/ask table when only the base fields are present', () => {
    renderWithProviders(<PriceBoardPanel symbol="VNM" />, {
      preloadedState: {
        priceBoard: {
          quotesBySymbol: {
            VNM: { symbol: 'VNM', time: '2026-09-03T00:00:00Z', price: 61900, change: 700, percentChange: 1.14, volume: 1 },
          },
        },
      },
    });

    expect(screen.getByText('61.900')).toBeInTheDocument();
    expect(screen.queryByText('Mua')).not.toBeInTheDocument();
  });

  it('shows ceiling/floor/reference and the bid/ask book when the full quote is present', () => {
    renderWithProviders(<PriceBoardPanel symbol="VNM" />, {
      preloadedState: {
        priceBoard: {
          quotesBySymbol: {
            VNM: {
              symbol: 'VNM',
              time: '2026-09-03T00:00:00Z',
              price: 61900,
              change: 700,
              percentChange: 1.14,
              volume: 1,
              ceilingPrice: 65400,
              floorPrice: 57000,
              referencePrice: 61200,
              bids: [{ price: 61800, volume: 100 }],
              asks: [{ price: 61900, volume: 200 }],
              foreignRoom: 1000,
              foreignBuyVolume: 50,
              foreignSellVolume: 20,
            },
          },
        },
      },
    });

    expect(screen.getByText('65.400')).toBeInTheDocument();
    expect(screen.getByText('57.000')).toBeInTheDocument();
    expect(screen.getByText('61.200')).toBeInTheDocument();
    expect(screen.getByText('Mua')).toBeInTheDocument();
    expect(screen.getByText(/room ngoại/i)).toBeInTheDocument();
  });
});

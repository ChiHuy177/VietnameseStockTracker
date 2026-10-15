import * as marketIndexService from '@/services/marketIndexService';
import { renderWithProviders } from '@/test/renderWithProviders';
import { screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { IndexTicker } from './IndexTicker';

vi.mock('@/services/marketIndexService');

const QUOTES = [
  { symbol: 'VNINDEX', name: 'VN-INDEX', value: 1258.47, change: 9.82, percentChange: 0.79, time: '2026-10-11T03:32:15Z' },
  { symbol: 'VN30', name: 'VN30', value: 1331.92, change: 6.15, percentChange: 0.47, time: '2026-10-11T03:32:15Z' },
  { symbol: 'HNXINDEX', name: 'HNX-INDEX', value: 238.64, change: -1.12, percentChange: -0.47, time: '2026-10-11T03:32:15Z' },
  { symbol: 'UPCOMINDEX', name: 'UPCOM-INDEX', value: 92.85, change: 0, percentChange: 0, time: '2026-10-11T03:32:15Z' },
];

describe('IndexTicker', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(marketIndexService.getIndexOhlcvHistory).mockResolvedValue({ symbol: 'VNINDEX', bars: [] });
  });

  it('renders every index returned by the API with up/down coloring', async () => {
    vi.mocked(marketIndexService.getMarketIndices).mockResolvedValue(QUOTES);

    renderWithProviders(<IndexTicker />);

    expect(await screen.findByText('VN-INDEX')).toBeInTheDocument();
    expect(screen.getByText('VN30')).toBeInTheDocument();
    expect(screen.getByText('HNX-INDEX')).toBeInTheDocument();
    expect(screen.getByText('UPCOM-INDEX')).toBeInTheDocument();

    expect(screen.getByText(/\+0\.79%/)).toHaveClass('text-up');
    expect(screen.getByText(/-0\.47%/)).toHaveClass('text-down');
    expect(screen.getByText(/0\.00%/)).toHaveClass('text-muted-foreground');
  });

  it('renders a sparkline from the index OHLCV endpoint', async () => {
    vi.mocked(marketIndexService.getMarketIndices).mockResolvedValue([QUOTES[0]]);
    vi.mocked(marketIndexService.getIndexOhlcvHistory).mockResolvedValue({
      symbol: 'VNINDEX',
      bars: [
        { time: '2026-10-09T00:00:00Z', open: 1240, high: 1252, low: 1238, close: 1248.65, volume: 100 },
        { time: '2026-10-10T00:00:00Z', open: 1248, high: 1260, low: 1246, close: 1258.47, volume: 100 },
      ],
    });

    const { container } = renderWithProviders(<IndexTicker />);

    expect(await screen.findByText('VN-INDEX')).toBeInTheDocument();
    await waitFor(() => expect(container.querySelector('svg polyline')).toBeInTheDocument());
    await waitFor(() =>
      expect(marketIndexService.getIndexOhlcvHistory).toHaveBeenCalledWith(
        'VNINDEX',
        expect.any(String),
        expect.any(String),
      ),
    );
  });

  // 404 trước phiên ingest đầu tiên là trạng thái bình thường của backend, không phải lỗi hạ tầng.
  it('shows a hint instead of an error when the indices endpoint fails', async () => {
    vi.mocked(marketIndexService.getMarketIndices).mockRejectedValue(new Error('404'));

    renderWithProviders(<IndexTicker />);

    expect(await screen.findByRole('alert')).toHaveTextContent(/job ingest chỉ số chưa chạy/i);
  });
});

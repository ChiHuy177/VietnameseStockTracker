import { OhlcvChart } from '@/features/chart/OhlcvChart';
import { PriceBoardPanel } from '@/features/priceBoard/PriceBoardPanel';
import { useOhlcvHistory } from '@/features/stockDetail/useOhlcvHistory';
import { Link, useParams } from 'react-router-dom';

function isoDateDaysAgo(days: number): string {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date.toISOString().slice(0, 10);
}

const HISTORY_LOOKBACK_DAYS = 90;

export function StockDetailPage() {
  const { symbol = '' } = useParams<{ symbol: string }>();
  const start = isoDateDaysAgo(HISTORY_LOOKBACK_DAYS);
  const end = isoDateDaysAgo(0);
  const { data: history, isLoading, isError } = useOhlcvHistory(symbol, start, end);

  return (
    <div className="flex flex-col gap-4">
      <Link to="/" className="text-muted-foreground text-sm hover:underline">
        ← Watchlist
      </Link>
      <h2 className="text-xl font-semibold">{symbol}</h2>

      <PriceBoardPanel symbol={symbol} />

      {isLoading && <p className="text-muted-foreground text-sm">Đang tải biểu đồ...</p>}
      {isError && (
        <p role="alert" className="text-destructive text-sm">
          Không tải được lịch sử giá.
        </p>
      )}
      {history && history.bars.length === 0 && (
        <p className="text-muted-foreground text-sm">Chưa có dữ liệu lịch sử cho mã này.</p>
      )}
      {history && history.bars.length > 0 && <OhlcvChart bars={history.bars} />}
    </div>
  );
}

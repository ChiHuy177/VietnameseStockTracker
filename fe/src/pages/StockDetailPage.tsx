import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { OhlcvChart } from '@/features/chart/OhlcvChart';
import { PriceBoardPanel } from '@/features/priceBoard/PriceBoardPanel';
import { useOhlcvHistory } from '@/features/stockDetail/useOhlcvHistory';
import { isoDateDaysAgo } from '@/lib/date';
import { ArrowLeft, CandlestickChart } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';

const HISTORY_LOOKBACK_DAYS = 90;

export function StockDetailPage() {
  const { symbol = '' } = useParams<{ symbol: string }>();
  const start = isoDateDaysAgo(HISTORY_LOOKBACK_DAYS);
  const end = isoDateDaysAgo(0);
  const { data: history, isLoading, isError } = useOhlcvHistory(symbol, start, end);

  return (
    <div className="flex flex-col gap-6">
      <Link to="/" className="text-muted-foreground flex w-fit items-center gap-1 text-sm hover:underline">
        <ArrowLeft className="h-3.5 w-3.5" />
        Watchlist
      </Link>

      <Card>
        <CardHeader>
          <h2 className="text-xl font-semibold">{symbol}</h2>
        </CardHeader>
        <CardContent>
          <PriceBoardPanel symbol={symbol} />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2.5 text-base">
            <span className="bg-primary/10 text-primary rounded-md p-1.5">
              <CandlestickChart className="h-4 w-4" />
            </span>
            Biểu đồ giá (90 ngày)
          </CardTitle>
        </CardHeader>
        <CardContent>
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
        </CardContent>
      </Card>
    </div>
  );
}

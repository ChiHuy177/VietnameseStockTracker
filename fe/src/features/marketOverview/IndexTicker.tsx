import { Sparkline } from '@/components/ui/sparkline';
import type { IndexQuote } from '@/models/marketIndex';
import { Minus, TrendingDown, TrendingUp } from 'lucide-react';
import type { ReactNode } from 'react';
import { useIndexSparkline } from './useIndexSparkline';
import { useMarketIndices } from './useMarketIndices';

const SKELETON_CELL_COUNT = 4;

function IndexCell({ index }: { index: IndexQuote }) {
  const { data: sparklineValues } = useIndexSparkline(index.symbol);

  const isUp = index.change > 0;
  const isDown = index.change < 0;
  const colorClass = isUp ? 'text-up' : isDown ? 'text-down' : 'text-muted-foreground';
  const sparklineColor = isUp ? 'stroke-up' : isDown ? 'stroke-down' : 'stroke-muted-foreground';
  const sign = index.change > 0 ? '+' : '';
  const Icon = isUp ? TrendingUp : isDown ? TrendingDown : Minus;

  return (
    <div className="bg-card flex flex-col gap-2 px-4 py-3">
      <span className="display text-muted-foreground text-[11px]">{index.name}</span>
      <div className="flex items-center justify-between gap-3">
        <span className={`num text-xl font-semibold ${colorClass}`}>{index.value.toLocaleString('vi-VN')}</span>
        {sparklineValues && sparklineValues.length > 1 && (
          <Sparkline values={sparklineValues} strokeClassName={sparklineColor} />
        )}
      </div>
      <span className={`num flex items-center gap-1 text-xs font-medium ${colorClass}`}>
        <Icon className="h-3.5 w-3.5" />
        {sign}
        {index.change.toLocaleString('vi-VN')} ({sign}
        {index.percentChange.toFixed(2)}%)
      </span>
    </div>
  );
}

function TickerGrid({ children }: { children: ReactNode }) {
  return (
    <div className="bg-border grid grid-cols-2 gap-px overflow-hidden rounded-xl border sm:grid-cols-4">{children}</div>
  );
}

export function IndexTicker() {
  const { data: indices, isLoading, isError } = useMarketIndices();

  if (isLoading) {
    return (
      <TickerGrid>
        {Array.from({ length: SKELETON_CELL_COUNT }, (_, cell) => (
          <div key={cell} className="bg-card flex flex-col gap-2 px-4 py-3" aria-hidden="true">
            <div className="bg-muted h-3 w-20 animate-pulse rounded" />
            <div className="bg-muted h-6 w-24 animate-pulse rounded" />
            <div className="bg-muted h-3 w-28 animate-pulse rounded" />
          </div>
        ))}
      </TickerGrid>
    );
  }

  // Backend trả 404 khi chưa có bar nào trong `price_history_ohlcv` cho 1 chỉ số — tức job
  // `IndexIngestionService` (cron 18:10 T2-T6) chưa chạy phiên nào, không phải lỗi hạ tầng.
  if (isError) {
    return (
      <p role="alert" className="text-muted-foreground rounded-xl border border-dashed p-4 text-sm">
        Chưa tải được chỉ số thị trường — có thể job ingest chỉ số chưa chạy phiên nào.
      </p>
    );
  }

  if (!indices || indices.length === 0) {
    return (
      <p className="text-muted-foreground rounded-xl border border-dashed p-4 text-sm">Chưa có dữ liệu chỉ số.</p>
    );
  }

  return (
    <TickerGrid>
      {indices.map((index) => (
        <IndexCell key={index.symbol} index={index} />
      ))}
    </TickerGrid>
  );
}

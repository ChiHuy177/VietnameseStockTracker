import { useAppSelector } from '@/app/hooks';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Sparkline } from '@/components/ui/sparkline';
import { selectQuote } from '@/features/priceBoard/priceBoardSlice';
import type { WatchlistItem } from '@/models/watchlist';
import { Minus, TrendingDown, TrendingUp, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useSparklineData } from './useSparklineData';
import { useRemoveFromWatchlist, useWatchlist } from './useWatchlist';

function WatchlistRow({ item }: { item: WatchlistItem }) {
  const quote = useAppSelector(selectQuote(item.symbol));
  const removeFromWatchlist = useRemoveFromWatchlist();
  const { data: sparklineValues } = useSparklineData(item.symbol);

  const isUp = quote !== undefined && quote.change > 0;
  const isDown = quote !== undefined && quote.change < 0;
  const colorClass = isUp ? 'text-green-600' : isDown ? 'text-red-600' : 'text-muted-foreground';
  const sparklineColor = isUp ? 'stroke-green-500' : isDown ? 'stroke-red-500' : 'stroke-muted-foreground';
  const sign = quote && quote.percentChange > 0 ? '+' : '';
  const Icon = isUp ? TrendingUp : isDown ? TrendingDown : Minus;

  return (
    <div className="group hover:bg-accent/50 flex items-center gap-3 rounded-lg px-2 py-3 transition-colors">
      <Link to={`/stocks/${item.symbol}`} className="flex min-w-0 flex-1 items-center gap-4">
        <div className="flex min-w-0 flex-col gap-0.5">
          <div className="flex items-center gap-2">
            <span className="font-semibold">{item.symbol}</span>
            <Badge variant="secondary" className="text-[10px]">
              {item.exchange}
            </Badge>
          </div>
          <span className="text-muted-foreground max-w-40 truncate text-xs">{item.name}</span>
        </div>

        <div className="hidden flex-1 justify-center sm:flex">
          {sparklineValues && sparklineValues.length > 1 && (
            <Sparkline values={sparklineValues} strokeClassName={sparklineColor} />
          )}
        </div>

        <div className="flex min-w-[100px] flex-col items-end gap-0.5">
          {quote ? (
            <>
              <span className={`font-semibold ${colorClass}`}>{quote.price.toLocaleString('vi-VN')}</span>
              <span className={`flex items-center gap-0.5 text-xs ${colorClass}`}>
                <Icon className="h-3 w-3" />
                {sign}
                {quote.percentChange.toFixed(2)}%
              </span>
            </>
          ) : (
            <span className="text-muted-foreground text-sm">—</span>
          )}
        </div>
      </Link>

      <Button
        variant="ghost"
        size="sm"
        className="opacity-0 transition-opacity group-hover:opacity-100"
        onClick={() => removeFromWatchlist.mutate(item.symbol)}
        disabled={removeFromWatchlist.isPending}
        aria-label={`Xóa ${item.symbol} khỏi watchlist`}
      >
        <X className="h-3.5 w-3.5" />
      </Button>
    </div>
  );
}

export function WatchlistTable() {
  const { data: items, isLoading, isError } = useWatchlist();

  if (isLoading) {
    return <p className="text-muted-foreground text-sm">Đang tải watchlist...</p>;
  }

  if (isError) {
    return (
      <p role="alert" className="text-destructive text-sm">
        Không tải được watchlist.
      </p>
    );
  }

  if (!items || items.length === 0) {
    return (
      <div className="text-muted-foreground rounded-lg border border-dashed py-8 text-center text-sm">
        Watchlist trống — tìm mã ở trên để thêm.
      </div>
    );
  }

  return (
    <div className="flex flex-col divide-y">
      {items.map((item) => (
        <WatchlistRow key={item.symbol} item={item} />
      ))}
    </div>
  );
}

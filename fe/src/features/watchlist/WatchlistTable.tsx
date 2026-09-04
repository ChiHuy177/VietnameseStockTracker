import { useAppSelector } from '@/app/hooks';
import { Button } from '@/components/ui/button';
import { selectQuote } from '@/features/priceBoard/priceBoardSlice';
import { Link } from 'react-router-dom';
import { useRemoveFromWatchlist, useWatchlist } from './useWatchlist';

function PriceCell({ symbol }: { symbol: string }) {
  const quote = useAppSelector(selectQuote(symbol));

  if (!quote) {
    return <span className="text-muted-foreground">—</span>;
  }

  const colorClass = quote.change > 0 ? 'text-green-600' : quote.change < 0 ? 'text-red-600' : 'text-muted-foreground';
  const sign = quote.percentChange > 0 ? '+' : '';

  return (
    <span className={colorClass}>
      {quote.price.toLocaleString('vi-VN')} ({sign}
      {quote.percentChange.toFixed(2)}%)
    </span>
  );
}

export function WatchlistTable() {
  const { data: items, isLoading, isError } = useWatchlist();
  const removeFromWatchlist = useRemoveFromWatchlist();

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
    return <p className="text-muted-foreground text-sm">Watchlist trống — tìm mã ở trên để thêm.</p>;
  }

  return (
    <table className="w-full text-left text-sm">
      <thead>
        <tr className="border-b">
          <th className="py-1">Mã</th>
          <th className="py-1">Tên</th>
          <th className="py-1">Sàn</th>
          <th className="py-1">Giá</th>
          <th className="py-1" />
        </tr>
      </thead>
      <tbody>
        {items.map((item) => (
          <tr key={item.symbol} className="border-b">
            <td className="py-1 font-medium">
              <Link to={`/stocks/${item.symbol}`} className="hover:underline">
                {item.symbol}
              </Link>
            </td>
            <td className="py-1">{item.name}</td>
            <td className="py-1">{item.exchange}</td>
            <td className="py-1">
              <PriceCell symbol={item.symbol} />
            </td>
            <td className="py-1 text-right">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => removeFromWatchlist.mutate(item.symbol)}
                disabled={removeFromWatchlist.isPending}
              >
                Xóa
              </Button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

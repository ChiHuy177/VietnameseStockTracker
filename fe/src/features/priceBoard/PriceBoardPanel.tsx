import { useAppSelector } from '@/app/hooks';
import { selectQuote } from './priceBoardSlice';

const CEILING_COLOR = 'text-fuchsia-600';
const FLOOR_COLOR = 'text-blue-600';
const REFERENCE_COLOR = 'text-yellow-600';

function formatNumber(value: number | undefined): string {
  return value !== undefined ? value.toLocaleString('vi-VN') : '—';
}

export function PriceBoardPanel({ symbol }: { symbol: string }) {
  const quote = useAppSelector(selectQuote(symbol));

  if (!quote) {
    return <p className="text-muted-foreground text-sm">Chưa có dữ liệu real-time cho mã này.</p>;
  }

  const changeColor =
    quote.change > 0 ? 'text-green-600' : quote.change < 0 ? 'text-red-600' : 'text-muted-foreground';
  const sign = quote.change > 0 ? '+' : '';

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-baseline gap-3">
        <span className={`text-3xl font-bold ${changeColor}`}>{formatNumber(quote.price)}</span>
        <span className={changeColor}>
          {sign}
          {quote.change.toLocaleString('vi-VN')} ({sign}
          {quote.percentChange.toFixed(2)}%)
        </span>
      </div>

      <div className="flex gap-6 text-sm">
        <span>
          Trần: <strong className={CEILING_COLOR}>{formatNumber(quote.ceilingPrice)}</strong>
        </span>
        <span>
          Sàn: <strong className={FLOOR_COLOR}>{formatNumber(quote.floorPrice)}</strong>
        </span>
        <span>
          TC: <strong className={REFERENCE_COLOR}>{formatNumber(quote.referencePrice)}</strong>
        </span>
      </div>

      {quote.bids && quote.asks && (
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b">
              <th className="py-1">Mua</th>
              <th className="py-1">KL</th>
              <th className="py-1">Bán</th>
              <th className="py-1">KL</th>
            </tr>
          </thead>
          <tbody>
            {quote.bids.map((bid, index) => {
              const ask = quote.asks?.[index];
              return (
                <tr key={index}>
                  <td className={FLOOR_COLOR}>{formatNumber(bid.price)}</td>
                  <td>{formatNumber(bid.volume)}</td>
                  <td className={CEILING_COLOR}>{formatNumber(ask?.price)}</td>
                  <td>{formatNumber(ask?.volume)}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}

      {quote.foreignRoom !== undefined && (
        <p className="text-muted-foreground text-sm">
          Room ngoại: {formatNumber(quote.foreignRoom)} (mua {formatNumber(quote.foreignBuyVolume)} / bán{' '}
          {formatNumber(quote.foreignSellVolume)})
        </p>
      )}
    </div>
  );
}

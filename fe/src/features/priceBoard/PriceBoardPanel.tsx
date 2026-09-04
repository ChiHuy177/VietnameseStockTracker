import { useAppSelector } from '@/app/hooks';
import { Minus, TrendingDown, TrendingUp } from 'lucide-react';
import { selectQuote } from './priceBoardSlice';

const CEILING_COLOR = 'text-fuchsia-600';
const FLOOR_COLOR = 'text-blue-600';
const REFERENCE_COLOR = 'text-yellow-600';

function formatNumber(value: number | undefined): string {
  return value !== undefined ? value.toLocaleString('vi-VN') : '—';
}

function StatTile({
  label,
  value,
  colorClass,
  bgClass,
}: {
  label: string;
  value: string;
  colorClass: string;
  bgClass: string;
}) {
  return (
    <div className={`flex flex-col items-center gap-0.5 rounded-lg py-2.5 ${bgClass}`}>
      <span className="text-muted-foreground text-xs">{label}</span>
      <span className={`font-semibold ${colorClass}`}>{value}</span>
    </div>
  );
}

export function PriceBoardPanel({ symbol }: { symbol: string }) {
  const quote = useAppSelector(selectQuote(symbol));

  if (!quote) {
    return (
      <div className="text-muted-foreground rounded-lg border border-dashed py-6 text-center text-sm">
        Chưa có dữ liệu real-time cho mã này.
      </div>
    );
  }

  const isUp = quote.change > 0;
  const isDown = quote.change < 0;
  const changeColor = isUp ? 'text-green-600' : isDown ? 'text-red-600' : 'text-muted-foreground';
  const sign = quote.change > 0 ? '+' : '';
  const Icon = isUp ? TrendingUp : isDown ? TrendingDown : Minus;

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-baseline gap-3">
        <span className={`text-4xl font-bold ${changeColor}`}>{formatNumber(quote.price)}</span>
        <span className={`flex items-center gap-1 font-medium ${changeColor}`}>
          <Icon className="h-4 w-4" />
          {sign}
          {quote.change.toLocaleString('vi-VN')} ({sign}
          {quote.percentChange.toFixed(2)}%)
        </span>
      </div>

      <div className="grid grid-cols-3 gap-2">
        <StatTile
          label="Trần"
          value={formatNumber(quote.ceilingPrice)}
          colorClass={CEILING_COLOR}
          bgClass="bg-fuchsia-500/10"
        />
        <StatTile
          label="Sàn"
          value={formatNumber(quote.floorPrice)}
          colorClass={FLOOR_COLOR}
          bgClass="bg-blue-500/10"
        />
        <StatTile
          label="Tham chiếu"
          value={formatNumber(quote.referencePrice)}
          colorClass={REFERENCE_COLOR}
          bgClass="bg-yellow-500/10"
        />
      </div>

      {quote.bids && quote.asks && (
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="text-muted-foreground border-b text-xs">
              <th className="py-1.5 font-medium">Mua</th>
              <th className="py-1.5 font-medium">KL</th>
              <th className="py-1.5 font-medium">Bán</th>
              <th className="py-1.5 font-medium">KL</th>
            </tr>
          </thead>
          <tbody>
            {quote.bids.map((bid, index) => {
              const ask = quote.asks?.[index];
              return (
                <tr key={index} className="border-b last:border-0">
                  <td className={`py-1 font-medium ${FLOOR_COLOR}`}>{formatNumber(bid.price)}</td>
                  <td className="text-muted-foreground py-1">{formatNumber(bid.volume)}</td>
                  <td className={`py-1 font-medium ${CEILING_COLOR}`}>{formatNumber(ask?.price)}</td>
                  <td className="text-muted-foreground py-1">{formatNumber(ask?.volume)}</td>
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

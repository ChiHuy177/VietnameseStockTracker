import { Sparkline } from '@/components/ui/sparkline';
import type { IndexQuote } from '@/models/marketIndex';
import { Minus, TrendingDown, TrendingUp } from 'lucide-react';
import { getIndexSparkline, getMarketIndices } from './marketOverviewMockData';

function IndexCell({ index }: { index: IndexQuote }) {
  const isUp = index.change > 0;
  const isDown = index.change < 0;
  const colorClass = isUp ? 'text-up' : isDown ? 'text-down' : 'text-muted-foreground';
  const sparklineColor = isUp ? 'stroke-up' : isDown ? 'stroke-down' : 'stroke-muted-foreground';
  const sign = index.change > 0 ? '+' : '';
  const Icon = isUp ? TrendingUp : isDown ? TrendingDown : Minus;
  const sparklineValues = getIndexSparkline(index.symbol);

  return (
    <div className="bg-card flex flex-col gap-2 px-4 py-3">
      <span className="display text-muted-foreground text-[11px]">{index.name}</span>
      <div className="flex items-center justify-between gap-3">
        <span className={`num text-xl font-semibold ${colorClass}`}>{index.value.toLocaleString('vi-VN')}</span>
        {sparklineValues.length > 1 && <Sparkline values={sparklineValues} strokeClassName={sparklineColor} />}
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

export function IndexTicker() {
  const indices = getMarketIndices();

  return (
    <div className="bg-border grid grid-cols-2 gap-px overflow-hidden rounded-xl border sm:grid-cols-4">
      {indices.map((index) => (
        <IndexCell key={index.symbol} index={index} />
      ))}
    </div>
  );
}

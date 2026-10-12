import { getMarketBreadth } from './marketOverviewMockData';

export function MarketBreadthBar() {
  const breadth = getMarketBreadth();
  const total = breadth.advancing + breadth.unchanged + breadth.declining;
  const advancingPct = (breadth.advancing / total) * 100;
  const unchangedPct = (breadth.unchanged / total) * 100;
  const decliningPct = (breadth.declining / total) * 100;

  return (
    <div className="bg-card flex flex-wrap items-center gap-6 rounded-xl border p-4">
      <div className="min-w-60 flex-1">
        <div className="bg-muted flex h-2.5 overflow-hidden rounded-full">
          <div className="bg-up h-full" style={{ width: `${advancingPct}%` }} />
          <div className="bg-ref h-full" style={{ width: `${unchangedPct}%` }} />
          <div className="bg-down h-full" style={{ width: `${decliningPct}%` }} />
        </div>
        <div className="mt-2.5 flex flex-wrap gap-5 text-xs">
          <span className="flex items-center gap-1.5">
            <span className="bg-up inline-block h-2 w-2 rounded-full" />
            <span className="text-muted-foreground">Tăng</span>
            <span className="num font-semibold">{breadth.advancing}</span>
          </span>
          <span className="flex items-center gap-1.5">
            <span className="bg-ref inline-block h-2 w-2 rounded-full" />
            <span className="text-muted-foreground">Đứng giá</span>
            <span className="num font-semibold">{breadth.unchanged}</span>
          </span>
          <span className="flex items-center gap-1.5">
            <span className="bg-down inline-block h-2 w-2 rounded-full" />
            <span className="text-muted-foreground">Giảm</span>
            <span className="num font-semibold">{breadth.declining}</span>
          </span>
        </div>
      </div>
      <div className="flex gap-2">
        <span className="bg-ceiling-soft text-ceiling num rounded-full px-3 py-1 text-xs font-bold">
          Trần · {breadth.ceiling}
        </span>
        <span className="bg-floor-soft text-floor num rounded-full px-3 py-1 text-xs font-bold">
          Sàn · {breadth.floor}
        </span>
      </div>
    </div>
  );
}

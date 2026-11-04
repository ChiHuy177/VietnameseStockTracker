import { computeTreemapLayout } from './treemapLayout';
import { useSectorSnapshots } from './useSectorSnapshots';

const LOGICAL_WIDTH = 1000;
const LOGICAL_HEIGHT = 300;
const NUM_ROWS = 4;

function cellClasses(percentChange: number): string {
  const magnitude = Math.abs(percentChange);
  const isUp = percentChange >= 0;
  if (magnitude >= 2) {
    return isUp ? 'bg-up text-white' : 'bg-down text-white';
  }
  if (magnitude >= 0.5) {
    return isUp ? 'bg-up/60 text-white' : 'bg-down/60 text-white';
  }
  return isUp ? 'bg-up-soft text-up' : 'bg-down-soft text-down';
}

export function SectorTreemap() {
  const { data: sectors, isLoading, isError } = useSectorSnapshots();

  if (isLoading) {
    return <div className="bg-muted h-75 w-full animate-pulse rounded-lg" aria-hidden="true" />;
  }

  if (isError || !sectors || sectors.length === 0) {
    return (
      <p role="alert" className="text-muted-foreground rounded-xl border border-dashed p-4 text-sm">
        Chưa tải được bản đồ ngành — có thể job snapshot thị trường chưa chạy phiên nào.
      </p>
    );
  }

  const items = sectors.map((sector) => ({ weight: sector.marketCapWeight, sector }));
  const rects = computeTreemapLayout(items, LOGICAL_WIDTH, LOGICAL_HEIGHT, NUM_ROWS);

  return (
    <div className="relative" style={{ paddingBottom: `${(LOGICAL_HEIGHT / LOGICAL_WIDTH) * 100}%` }}>
      <div className="absolute inset-0">
        {rects.map(({ item, x, y, width, height }) => (
          <div
            key={item.sector.code}
            data-sector-code={item.sector.code}
            className={`absolute flex flex-col justify-end gap-0.5 overflow-hidden p-1.5 ${cellClasses(item.sector.percentChange)}`}
            style={{
              left: `${(x / LOGICAL_WIDTH) * 100}%`,
              top: `${(y / LOGICAL_HEIGHT) * 100}%`,
              width: `${(width / LOGICAL_WIDTH) * 100}%`,
              height: `${(height / LOGICAL_HEIGHT) * 100}%`,
              border: '1px solid var(--card)',
            }}
          >
            <span className="display truncate text-[11px] leading-tight">{item.sector.name}</span>
            <span className="num text-[11px] font-bold">
              {item.sector.percentChange > 0 ? '+' : ''}
              {item.sector.percentChange.toFixed(1)}%
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

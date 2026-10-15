import { useSectorSnapshots } from './useSectorSnapshots';

const TOP_SECTOR_COUNT = 9;

export function SectorMoneyFlow() {
  const { data, isLoading, isError } = useSectorSnapshots();

  if (isLoading) {
    return <div className="bg-muted h-48 w-full animate-pulse rounded-lg" aria-hidden="true" />;
  }

  if (isError || !data || data.length === 0) {
    return (
      <p role="alert" className="text-muted-foreground rounded-xl border border-dashed p-4 text-sm">
        Chưa tải được dòng tiền theo ngành — có thể job snapshot thị trường chưa chạy phiên nào.
      </p>
    );
  }

  // Top N ngành theo |dòng tiền| — không có danh sách mã cố định như mock, dữ liệu thật thay đổi
  // theo từng phiên nên ngành nào nổi bật nhất là tuỳ phiên đó.
  const sectors = [...data].sort((a, b) => Math.abs(b.moneyFlow) - Math.abs(a.moneyFlow)).slice(0, TOP_SECTOR_COUNT);
  const maxAbs = Math.max(...sectors.map((sector) => Math.abs(sector.moneyFlow)));

  return (
    <div className="flex flex-col gap-2.5">
      {sectors.map((sector) => {
        const isPositive = sector.moneyFlow >= 0;
        const widthPct = (Math.abs(sector.moneyFlow) / maxAbs) * 48;

        return (
          <div key={sector.code} className="grid grid-cols-[88px_1fr_56px] items-center gap-2.5">
            <span className="text-muted-foreground truncate text-xs">{sector.name}</span>
            <span className="bg-muted relative block h-4 rounded">
              <span className="bg-border absolute top-[-3px] bottom-[-3px] left-1/2 w-px" />
              <span
                className={`absolute top-0.5 bottom-0.5 rounded-sm ${isPositive ? 'bg-up left-1/2' : 'bg-down right-1/2'}`}
                style={{ width: `${widthPct}%` }}
              />
            </span>
            <span className={`num text-right text-xs font-semibold ${isPositive ? 'text-up' : 'text-down'}`}>
              {isPositive ? '+' : ''}
              {sector.moneyFlow}
            </span>
          </div>
        );
      })}
    </div>
  );
}

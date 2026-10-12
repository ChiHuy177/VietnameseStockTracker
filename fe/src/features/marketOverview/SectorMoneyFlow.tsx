import { getSectorMoneyFlow } from './marketOverviewMockData';

export function SectorMoneyFlow() {
  const sectors = getSectorMoneyFlow();
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

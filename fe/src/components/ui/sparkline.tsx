const WIDTH = 100;
const HEIGHT = 32;

export function Sparkline({ values, strokeClassName = 'stroke-current' }: { values: number[]; strokeClassName?: string }) {
  if (values.length < 2) {
    return null;
  }

  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = max - min || 1;

  const points = values
    .map((value, index) => {
      const x = (index / (values.length - 1)) * WIDTH;
      const y = HEIGHT - ((value - min) / range) * HEIGHT;
      return `${x},${y}`;
    })
    .join(' ');

  return (
    <svg viewBox={`0 0 ${WIDTH} ${HEIGHT}`} className="h-8 w-24" preserveAspectRatio="none" aria-hidden="true">
      <polyline points={points} fill="none" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={strokeClassName} />
    </svg>
  );
}

import type { OhlcvBar } from '@/models/ohlcvBar';
import { CandlestickSeries, createChart, HistogramSeries, type ISeriesApi } from 'lightweight-charts';
import { useEffect, useRef } from 'react';
import { toCandlestickData, toVolumeData } from './ohlcvChartData';

// lightweight-charts' color parser only understands hex/rgb/hsl/named colors, not CSS color
// functions like oklch() — our tokens use oklch(), so resolve through the browser first, which
// normalizes any valid CSS color to rgb(...).
function resolveCssColor(value: string): string {
  const probe = document.createElement('div');
  probe.style.color = value;
  probe.style.display = 'none';
  document.body.appendChild(probe);
  const resolved = getComputedStyle(probe).color;
  document.body.removeChild(probe);
  return resolved;
}

export function OhlcvChart({ bars }: { bars: OhlcvBar[] }) {
  const containerRef = useRef<HTMLDivElement>(null);
  const candleSeriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);
  const volumeSeriesRef = useRef<ISeriesApi<'Histogram'> | null>(null);
  const colorsRef = useRef({ upColor: '', downColor: '' });

  useEffect(() => {
    if (!containerRef.current) {
      return;
    }

    // lightweight-charts draws to <canvas>, so it can't consume Tailwind utility classes like
    // the rest of the app — read the raw token values once at mount instead.
    const rootStyle = getComputedStyle(document.documentElement);
    const upColor = resolveCssColor(rootStyle.getPropertyValue('--up').trim());
    const downColor = resolveCssColor(rootStyle.getPropertyValue('--down').trim());
    colorsRef.current = { upColor, downColor };

    const chart = createChart(containerRef.current, {
      autoSize: true,
      layout: { background: { color: 'transparent' }, textColor: '#71717a' },
      grid: { vertLines: { visible: false }, horzLines: { visible: false } },
    });

    const candleSeries = chart.addSeries(CandlestickSeries, {
      upColor,
      downColor,
      borderVisible: false,
      wickUpColor: upColor,
      wickDownColor: downColor,
    });
    candleSeries.priceScale().applyOptions({ scaleMargins: { top: 0.1, bottom: 0.35 } });
    candleSeriesRef.current = candleSeries;

    const volumeSeries = chart.addSeries(HistogramSeries, {
      priceFormat: { type: 'volume' },
      priceScaleId: '',
    });
    volumeSeries.priceScale().applyOptions({ scaleMargins: { top: 0.7, bottom: 0 } });
    volumeSeriesRef.current = volumeSeries;

    return () => {
      chart.remove();
      candleSeriesRef.current = null;
      volumeSeriesRef.current = null;
    };
  }, []);

  useEffect(() => {
    const { upColor, downColor } = colorsRef.current;
    candleSeriesRef.current?.setData(toCandlestickData(bars));
    volumeSeriesRef.current?.setData(toVolumeData(bars, upColor, downColor));
  }, [bars]);

  return <div ref={containerRef} className="h-80 w-full" />;
}

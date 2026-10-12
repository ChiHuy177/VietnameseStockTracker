import type { OhlcvBar } from '@/models/ohlcvBar';
import { CandlestickSeries, createChart, HistogramSeries, type ISeriesApi } from 'lightweight-charts';
import { useEffect, useRef } from 'react';
import { toCandlestickData, toVolumeData } from './ohlcvChartData';

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
    const upColor = rootStyle.getPropertyValue('--up').trim();
    const downColor = rootStyle.getPropertyValue('--down').trim();
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

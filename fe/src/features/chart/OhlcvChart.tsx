import type { OhlcvBar } from '@/models/ohlcvBar';
import { CandlestickSeries, createChart, HistogramSeries, type ISeriesApi } from 'lightweight-charts';
import { useEffect, useRef } from 'react';
import { toCandlestickData, toVolumeData } from './ohlcvChartData';

export function OhlcvChart({ bars }: { bars: OhlcvBar[] }) {
  const containerRef = useRef<HTMLDivElement>(null);
  const candleSeriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);
  const volumeSeriesRef = useRef<ISeriesApi<'Histogram'> | null>(null);

  useEffect(() => {
    if (!containerRef.current) {
      return;
    }

    const chart = createChart(containerRef.current, {
      autoSize: true,
      layout: { background: { color: 'transparent' }, textColor: '#71717a' },
      grid: { vertLines: { visible: false }, horzLines: { visible: false } },
    });

    const candleSeries = chart.addSeries(CandlestickSeries, {
      upColor: '#16a34a',
      downColor: '#dc2626',
      borderVisible: false,
      wickUpColor: '#16a34a',
      wickDownColor: '#dc2626',
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
    candleSeriesRef.current?.setData(toCandlestickData(bars));
    volumeSeriesRef.current?.setData(toVolumeData(bars));
  }, [bars]);

  return <div ref={containerRef} className="h-80 w-full" />;
}

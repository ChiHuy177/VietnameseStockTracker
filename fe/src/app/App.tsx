import { useStompPriceBoard } from '@/features/priceBoard/useStompPriceBoard';
import { HomePage } from '@/pages/HomePage';
import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';

const StockDetailPage = lazy(() =>
  import('@/pages/StockDetailPage').then((module) => ({ default: module.StockDetailPage })),
);

export function App() {
  useStompPriceBoard();

  return (
    <main className="mx-auto max-w-4xl">
      <h1 className="mb-6 text-2xl font-bold">Vietnamese Stock Tracker</h1>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route
          path="/stocks/:symbol"
          element={
            <Suspense fallback={<p className="text-muted-foreground text-sm">Đang tải...</p>}>
              <StockDetailPage />
            </Suspense>
          }
        />
      </Routes>
    </main>
  );
}

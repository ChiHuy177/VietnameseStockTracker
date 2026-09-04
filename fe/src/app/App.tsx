import { useStompPriceBoard } from '@/features/priceBoard/useStompPriceBoard';
import { HomePage } from '@/pages/HomePage';
import { LineChart } from 'lucide-react';
import { lazy, Suspense } from 'react';
import { Link, Route, Routes } from 'react-router-dom';

const StockDetailPage = lazy(() =>
  import('@/pages/StockDetailPage').then((module) => ({ default: module.StockDetailPage })),
);

export function App() {
  useStompPriceBoard();

  return (
    <div className="min-h-screen">
      <header className="bg-primary text-primary-foreground">
        <div className="mx-auto flex max-w-7xl items-center gap-2 px-6 py-4 sm:px-10">
          <Link to="/" className="flex items-center gap-2">
            <LineChart className="h-6 w-6" />
            <span className="text-lg font-bold tracking-tight">Vietnamese Stock Tracker</span>
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-6 py-8 sm:px-10">
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
    </div>
  );
}

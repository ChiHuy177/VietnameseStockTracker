import { useStompPriceBoard } from '@/features/priceBoard/useStompPriceBoard';
import { HomePage } from '@/pages/HomePage';
import { lazy, Suspense } from 'react';
import { Link, Route, Routes } from 'react-router-dom';

const StockDetailPage = lazy(() =>
  import('@/pages/StockDetailPage').then((module) => ({ default: module.StockDetailPage })),
);

// 3 cột = 3 sàn app theo dõi (HOSE/HNX/UPCOM), cột cao nhất vuốt nhọn gợi đỉnh Landmark 81 —
// xem fe/public/favicon.svg cho bản favicon (cùng hình, có nền navy bo góc).
function VstMark({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 64 64" fill="currentColor" aria-hidden="true">
      <rect x="13" y="36" width="10" height="18" rx="2" />
      <rect x="27" y="26" width="10" height="28" rx="2" />
      <rect x="41" y="14" width="10" height="40" rx="2" />
      <polygon points="41,14 51,14 46,6" />
      <rect x="9" y="57" width="46" height="2.4" rx="1.2" />
    </svg>
  );
}

export function App() {
  useStompPriceBoard();

  return (
    <div className="min-h-screen">
      <header className="bg-primary text-primary-foreground">
        <div className="mx-auto flex max-w-7xl items-center gap-2 px-6 py-4 sm:px-10">
          <Link to="/" className="flex items-center gap-2.5">
            <VstMark className="h-7 w-7 shrink-0" />
            <span className="flex items-baseline gap-2">
              <span className="num text-lg font-bold tracking-wide">
                VST
                <span
                  className="bg-current align-middle ml-0.5 inline-block h-[0.85em] w-[0.5em] translate-y-[0.1em] animate-pulse"
                  aria-hidden="true"
                />
              </span>
              <span className="hidden text-sm font-medium tracking-tight sm:inline">Vietnamese Stock Tracker</span>
            </span>
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

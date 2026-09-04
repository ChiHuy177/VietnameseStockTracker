import { SearchBox } from '@/features/search/SearchBox';
import { WatchlistTable } from '@/features/watchlist/WatchlistTable';

export function HomePage() {
  return (
    <div className="flex flex-col gap-8">
      <section>
        <h2 className="mb-2 text-lg font-semibold">Tìm mã cổ phiếu</h2>
        <SearchBox />
      </section>
      <section>
        <h2 className="mb-2 text-lg font-semibold">Watchlist</h2>
        <WatchlistTable />
      </section>
    </div>
  );
}

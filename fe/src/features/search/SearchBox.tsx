import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useDebouncedValue } from '@/hooks/useDebouncedValue';
import { useAddToWatchlist } from '@/features/watchlist/useWatchlist';
import type { ChangeEvent } from 'react';
import { useState } from 'react';
import { useSearchStocks } from './useSearchStocks';

export function SearchBox() {
  const [query, setQuery] = useState('');
  const debouncedQuery = useDebouncedValue(query, 300);
  const { data: results, isFetching, isError } = useSearchStocks(debouncedQuery);
  const addToWatchlist = useAddToWatchlist();

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    setQuery(event.target.value);
  }

  const trimmedQuery = query.trim();

  return (
    <div className="flex flex-col gap-2">
      <Input placeholder="Tìm mã hoặc tên công ty..." value={query} onChange={handleChange} />

      {isFetching && <p className="text-muted-foreground text-sm">Đang tìm...</p>}
      {isError && (
        <p role="alert" className="text-destructive text-sm">
          Không tìm được, thử lại sau.
        </p>
      )}
      {results && results.length === 0 && trimmedQuery !== '' && !isFetching && (
        <p className="text-muted-foreground text-sm">Không có kết quả.</p>
      )}

      <ul className="flex flex-col gap-1">
        {results?.map((stock) => (
          <li key={stock.symbol} className="flex items-center justify-between gap-2 border-b py-1">
            <span>
              <strong>{stock.symbol}</strong> — {stock.name} ({stock.exchange})
            </span>
            <Button
              size="sm"
              onClick={() => addToWatchlist.mutate(stock.symbol)}
              disabled={addToWatchlist.isPending}
            >
              + Watchlist
            </Button>
          </li>
        ))}
      </ul>
    </div>
  );
}

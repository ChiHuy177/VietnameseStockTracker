import { searchStocks } from '@/services/stockService';
import { useQuery } from '@tanstack/react-query';

export function useSearchStocks(query: string) {
  return useQuery({
    queryKey: ['stocks', 'search', query],
    queryFn: () => searchStocks(query),
    enabled: query.trim().length > 0,
  });
}

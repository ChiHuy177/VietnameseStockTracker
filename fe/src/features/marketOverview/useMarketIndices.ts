import { getMarketIndices } from '@/services/marketIndexService';
import { useQuery } from '@tanstack/react-query';

export function useMarketIndices() {
  return useQuery({
    queryKey: ['indices'],
    queryFn: getMarketIndices,
  });
}

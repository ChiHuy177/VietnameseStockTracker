import { getMarketBreadth } from '@/services/marketOverviewService';
import { useQuery } from '@tanstack/react-query';

export function useMarketBreadth() {
  return useQuery({
    queryKey: ['market', 'breadth'],
    queryFn: getMarketBreadth,
  });
}

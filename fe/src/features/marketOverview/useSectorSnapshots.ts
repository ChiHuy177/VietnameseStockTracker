import { getSectorSnapshots } from '@/services/marketOverviewService';
import { useQuery } from '@tanstack/react-query';

export function useSectorSnapshots() {
  return useQuery({
    queryKey: ['market', 'sectors'],
    queryFn: getSectorSnapshots,
  });
}

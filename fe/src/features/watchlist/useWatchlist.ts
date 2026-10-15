import { addToWatchlist, getWatchlist, removeFromWatchlist } from '@/services/watchlistService';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

const WATCHLIST_KEY = ['watchlist'];

export function useWatchlist() {
  return useQuery({
    queryKey: WATCHLIST_KEY,
    queryFn: getWatchlist,
  });
}

export function useAddToWatchlist() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: addToWatchlist,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: WATCHLIST_KEY }),
  });
}

export function useRemoveFromWatchlist() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: removeFromWatchlist,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: WATCHLIST_KEY }),
  });
}

import { priceBoardReducer } from '@/features/priceBoard/priceBoardSlice';
import { configureStore } from '@reduxjs/toolkit';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import type { ReactElement } from 'react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';

function createTestStore(preloadedState?: { priceBoard: ReturnType<typeof priceBoardReducer> }) {
  return configureStore({
    reducer: { priceBoard: priceBoardReducer },
    preloadedState,
  });
}

export function renderWithProviders(
  ui: ReactElement,
  options?: {
    preloadedState?: { priceBoard: ReturnType<typeof priceBoardReducer> };
    route?: string;
  },
) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
  const store = createTestStore(options?.preloadedState);

  return render(
    <QueryClientProvider client={queryClient}>
      <Provider store={store}>
        <MemoryRouter initialEntries={[options?.route ?? '/']}>{ui}</MemoryRouter>
      </Provider>
    </QueryClientProvider>,
  );
}

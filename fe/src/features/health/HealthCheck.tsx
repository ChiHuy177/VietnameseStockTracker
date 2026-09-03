import { useEffect, useState } from 'react';
import { fetchHealth } from './api';
import type { HealthResponse } from './types';

type State =
  | { kind: 'loading' }
  | { kind: 'success'; data: HealthResponse }
  | { kind: 'error'; message: string };

export function HealthCheck() {
  const [state, setState] = useState<State>({ kind: 'loading' });

  useEffect(() => {
    fetchHealth()
      .then((data) => setState({ kind: 'success', data }))
      .catch((error: unknown) =>
        setState({
          kind: 'error',
          message: error instanceof Error ? error.message : 'Unknown error',
        }),
      );
  }, []);

  if (state.kind === 'loading') {
    return <p>Checking backend health…</p>;
  }

  if (state.kind === 'error') {
    return <p role="alert">Backend unreachable: {state.message}</p>;
  }

  return (
    <p>
      Backend status: <strong>{state.data.status}</strong> (checked at{' '}
      {new Date(state.data.checkedAt).toLocaleString()})
    </p>
  );
}

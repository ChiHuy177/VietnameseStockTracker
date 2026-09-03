import axios from 'axios';
import { useEffect, useState } from 'react';
import type { HealthStatus } from '../../models/health';
import { getHealth } from '../../services/healthService';

type State =
  | { kind: 'loading' }
  | { kind: 'success'; data: HealthStatus }
  | { kind: 'error'; message: string };

function toErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return (error.response?.data as { detail?: string } | undefined)?.detail ?? error.message;
  }
  return error instanceof Error ? error.message : 'Unknown error';
}

export function HealthCheck() {
  const [state, setState] = useState<State>({ kind: 'loading' });

  useEffect(() => {
    getHealth()
      .then((data) => setState({ kind: 'success', data }))
      .catch((error: unknown) => setState({ kind: 'error', message: toErrorMessage(error) }));
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

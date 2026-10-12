import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { IndexTicker } from './IndexTicker';

describe('IndexTicker', () => {
  it('renders all 4 market indices with up/down coloring', () => {
    render(<IndexTicker />);

    expect(screen.getByText('VN-INDEX')).toBeInTheDocument();
    expect(screen.getByText('VN30')).toBeInTheDocument();
    expect(screen.getByText('HNX-INDEX')).toBeInTheDocument();
    expect(screen.getByText('UPCOM-INDEX')).toBeInTheDocument();

    expect(screen.getByText(/\+0\.79%/)).toHaveClass('text-up');
    expect(screen.getByText(/-0\.47%/)).toHaveClass('text-down');
  });
});

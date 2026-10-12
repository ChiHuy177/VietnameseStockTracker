import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { MarketBreadthBar } from './MarketBreadthBar';

describe('MarketBreadthBar', () => {
  it('renders advancing/unchanged/declining counts and ceiling/floor chips', () => {
    render(<MarketBreadthBar />);

    expect(screen.getByText('412')).toBeInTheDocument();
    expect(screen.getByText('186')).toBeInTheDocument();
    expect(screen.getByText('298')).toBeInTheDocument();
    expect(screen.getByText(/Trần/)).toHaveTextContent('18');
    expect(screen.getByText(/Sàn/)).toHaveTextContent('7');
  });
});

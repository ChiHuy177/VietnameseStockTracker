import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { SectorMoneyFlow } from './SectorMoneyFlow';

describe('SectorMoneyFlow', () => {
  it('renders sector names and signed values with up/down coloring', () => {
    render(<SectorMoneyFlow />);

    expect(screen.getByText('Ngân hàng')).toBeInTheDocument();
    expect(screen.getByText('+842')).toHaveClass('text-up');
    expect(screen.getByText('-304')).toHaveClass('text-down');
  });
});

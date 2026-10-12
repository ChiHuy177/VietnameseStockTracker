import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { SectorTreemap } from './SectorTreemap';

describe('SectorTreemap', () => {
  it('renders every sector and sizes the largest sector wider than the smallest', () => {
    const { container } = render(<SectorTreemap />);

    expect(screen.getByText('Ngân hàng')).toBeInTheDocument();
    expect(screen.getByText('Dệt may')).toBeInTheDocument();

    const cells = container.querySelectorAll('[data-sector-code]');
    expect(cells).toHaveLength(14);

    const nganHang = container.querySelector('[data-sector-code="NH"]') as HTMLElement;
    const detMay = container.querySelector('[data-sector-code="DM"]') as HTMLElement;

    expect(parseFloat(nganHang.style.width)).toBeGreaterThan(parseFloat(detMay.style.width));
  });
});

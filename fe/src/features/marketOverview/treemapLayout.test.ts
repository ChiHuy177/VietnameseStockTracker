import { describe, expect, it } from 'vitest';
import { computeTreemapLayout } from './treemapLayout';

describe('computeTreemapLayout', () => {
  it('returns an empty array for no items', () => {
    expect(computeTreemapLayout([], 1000, 300, 4)).toEqual([]);
  });

  it('gives a single item the full container', () => {
    const result = computeTreemapLayout([{ weight: 10 }], 1000, 300, 4);

    expect(result).toEqual([{ item: { weight: 10 }, x: 0, y: 0, width: 1000, height: 300 }]);
  });

  it('splits evenly-weighted items into rows of equal height', () => {
    const items = [{ weight: 1 }, { weight: 1 }, { weight: 1 }, { weight: 1 }];
    const result = computeTreemapLayout(items, 1000, 400, 4);

    expect(result).toHaveLength(4);
    result.forEach((rect) => expect(rect.height).toBeCloseTo(100));
  });

  it('gives a row with roughly 2x the weight roughly 2x the height', () => {
    const items = [{ weight: 2 }, { weight: 1 }];
    const result = computeTreemapLayout(items, 1000, 300, 2);

    const heavy = result.find((rect) => rect.item.weight === 2)!;
    const light = result.find((rect) => rect.item.weight === 1)!;

    expect(heavy.height).toBeCloseTo(200);
    expect(light.height).toBeCloseTo(100);
  });

  it('clamps numRows so it never exceeds the item count (no empty rows)', () => {
    const items = [{ weight: 1 }, { weight: 1 }];
    const result = computeTreemapLayout(items, 1000, 300, 10);

    expect(result).toHaveLength(2);
    const rowCount = new Set(result.map((rect) => rect.y)).size;
    expect(rowCount).toBeLessThanOrEqual(2);
  });

  it('tiles the full container (row heights sum to containerHeight, row widths sum to containerWidth)', () => {
    const items = [
      { weight: 24 }, { weight: 16 }, { weight: 10 }, { weight: 8 }, { weight: 7 }, { weight: 7 },
      { weight: 6 }, { weight: 6 }, { weight: 5 }, { weight: 5 }, { weight: 4 }, { weight: 4 },
      { weight: 4 }, { weight: 3 },
    ];
    const result = computeTreemapLayout(items, 1000, 300, 4);

    const rowsByY = new Map<number, typeof result>();
    for (const rect of result) {
      rowsByY.set(rect.y, [...(rowsByY.get(rect.y) ?? []), rect]);
    }

    const totalHeight = [...rowsByY.values()].reduce((sum, row) => sum + row[0].height, 0);
    expect(totalHeight).toBeCloseTo(300, 5);

    for (const row of rowsByY.values()) {
      const rowWidth = row.reduce((sum, rect) => sum + rect.width, 0);
      expect(rowWidth).toBeCloseTo(1000, 5);
    }
  });
});

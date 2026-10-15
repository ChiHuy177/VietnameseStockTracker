export interface TreemapRect<T> {
  item: T;
  x: number;
  y: number;
  width: number;
  height: number;
}

/**
 * Simplified "greedy rows" treemap: buckets items (sorted by weight desc) into `numRows` rows of
 * roughly equal weight-sum, sizes each row's height by its weight share of the total, and each
 * cell's width by its weight share within its row. Not a fully squarified treemap — approximate
 * box shapes are fine for this use case (sector weights visualization), and the simpler math
 * keeps this pure/deterministic/easy to test.
 */
export function computeTreemapLayout<T extends { weight: number }>(
  items: T[],
  containerWidth: number,
  containerHeight: number,
  numRows: number,
): TreemapRect<T>[] {
  if (items.length === 0) {
    return [];
  }

  const sorted = [...items].sort((a, b) => b.weight - a.weight);
  const effectiveRows = Math.max(1, Math.min(numRows, sorted.length));
  const totalWeight = sorted.reduce((sum, item) => sum + item.weight, 0);
  const targetRowWeight = totalWeight / effectiveRows;

  const rows: T[][] = [];
  let currentRow: T[] = [];
  let currentWeight = 0;

  for (const item of sorted) {
    currentRow.push(item);
    currentWeight += item.weight;
    if (currentWeight >= targetRowWeight && rows.length < effectiveRows - 1) {
      rows.push(currentRow);
      currentRow = [];
      currentWeight = 0;
    }
  }
  if (currentRow.length > 0) {
    rows.push(currentRow);
  }

  const rects: TreemapRect<T>[] = [];
  let y = 0;
  for (const row of rows) {
    const rowWeight = row.reduce((sum, item) => sum + item.weight, 0);
    const height = (rowWeight / totalWeight) * containerHeight;
    let x = 0;
    for (const item of row) {
      const width = (item.weight / rowWeight) * containerWidth;
      rects.push({ item, x, y, width, height });
      x += width;
    }
    y += height;
  }

  return rects;
}

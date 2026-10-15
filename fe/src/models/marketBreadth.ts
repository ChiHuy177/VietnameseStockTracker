export interface MarketBreadthSnapshot {
  advancing: number;
  declining: number;
  unchanged: number;
  ceiling: number;
  floor: number;
  time: string;
}

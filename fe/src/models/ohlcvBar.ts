export interface OhlcvBar {
  time: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface OhlcvHistory {
  symbol: string;
  bars: OhlcvBar[];
}

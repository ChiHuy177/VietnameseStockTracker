export interface PriceLevel {
  price: number;
  volume: number;
}

export interface PriceQuote {
  symbol: string;
  time: string;
  price: number;
  change: number;
  percentChange: number;
  volume: number;
  // Các field dưới đây CHƯA được backend gửi (xem CLAUDE.md, mục "Việc đang chờ: backend").
  // Optional có chủ đích — UI tự hiển thị khi backend bổ sung, không cần đổi FE.
  referencePrice?: number;
  ceilingPrice?: number;
  floorPrice?: number;
  bids?: PriceLevel[];
  asks?: PriceLevel[];
  foreignBuyVolume?: number;
  foreignSellVolume?: number;
  foreignRoom?: number;
}

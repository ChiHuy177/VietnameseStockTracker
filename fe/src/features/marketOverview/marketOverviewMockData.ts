// Dữ liệu minh họa (mock) — chưa có endpoint backend nào cho chỉ số/ngành/độ rộng thị trường
// (xem CLAUDE.md, mục "Trang chủ kiểu Fireant"). Các hàm accessor dưới đây trả đồng bộ, không
// phải Promise — khi nối API thật, phía gọi (component) cần đổi sang useQuery, không chỉ đổi
// nội dung hàm này.
import type { MarketBreadthSnapshot } from '@/models/marketBreadth';
import type { IndexQuote } from '@/models/marketIndex';
import type { SectorSnapshot } from '@/models/sector';

const SNAPSHOT_TIME = '2026-10-11T03:32:15Z';

const INDICES: IndexQuote[] = [
  { symbol: 'VNINDEX', name: 'VN-INDEX', value: 1258.47, change: 9.82, percentChange: 0.79, time: SNAPSHOT_TIME },
  { symbol: 'VN30', name: 'VN30', value: 1331.92, change: 6.15, percentChange: 0.47, time: SNAPSHOT_TIME },
  { symbol: 'HNXINDEX', name: 'HNX-INDEX', value: 238.64, change: -1.12, percentChange: -0.47, time: SNAPSHOT_TIME },
  { symbol: 'UPCOMINDEX', name: 'UPCOM-INDEX', value: 92.85, change: 0.18, percentChange: 0.19, time: SNAPSHOT_TIME },
];

const INDEX_SPARKLINES: Record<string, number[]> = {
  VNINDEX: [1231, 1236, 1229, 1241, 1238, 1247, 1244, 1252, 1249, 1258],
  VN30: [1312, 1318, 1309, 1322, 1319, 1325, 1321, 1328, 1326, 1332],
  HNXINDEX: [241, 240, 242, 239, 240, 238, 239, 237, 238, 239],
  UPCOMINDEX: [92.1, 92.3, 92.0, 92.5, 92.4, 92.6, 92.5, 92.7, 92.6, 92.9],
};

const BREADTH: MarketBreadthSnapshot = {
  advancing: 412,
  declining: 298,
  unchanged: 186,
  ceiling: 18,
  floor: 7,
  time: SNAPSHOT_TIME,
};

// 1 danh sách ngành dùng chung cho cả treemap (tất cả) và dòng tiền (lọc theo |moneyFlow| lớn nhất).
const SECTORS: SectorSnapshot[] = [
  { code: 'NH', name: 'Ngân hàng', marketCapWeight: 24, percentChange: 1.2, moneyFlow: 842 },
  { code: 'BDS', name: 'Bất động sản', marketCapWeight: 16, percentChange: -0.8, moneyFlow: -304 },
  { code: 'TPDU', name: 'Thực phẩm & Đồ uống', marketCapWeight: 10, percentChange: 0.4, moneyFlow: 42 },
  { code: 'THEP', name: 'Thép & Khoáng sản', marketCapWeight: 8, percentChange: 2.1, moneyFlow: 298 },
  { code: 'CK', name: 'Chứng khoán', marketCapWeight: 7, percentChange: 1.8, moneyFlow: 151 },
  { code: 'DK', name: 'Dầu khí', marketCapWeight: 7, percentChange: -1.5, moneyFlow: -189 },
  { code: 'BL', name: 'Bán lẻ', marketCapWeight: 6, percentChange: 0.6, moneyFlow: 64 },
  { code: 'XD', name: 'Xây dựng & VLXD', marketCapWeight: 6, percentChange: -0.3, moneyFlow: -121 },
  { code: 'CNTT', name: 'Công nghệ thông tin', marketCapWeight: 5, percentChange: 3.4, moneyFlow: 365 },
  { code: 'HC', name: 'Hóa chất', marketCapWeight: 5, percentChange: -0.6, moneyFlow: -58 },
  { code: 'BH', name: 'Bảo hiểm', marketCapWeight: 4, percentChange: 0.2, moneyFlow: 12 },
  { code: 'VT', name: 'Vận tải & Logistics', marketCapWeight: 4, percentChange: -1.1, moneyFlow: -34 },
  { code: 'DIEN', name: 'Điện & Năng lượng', marketCapWeight: 4, percentChange: 0.9, moneyFlow: 28 },
  { code: 'DM', name: 'Dệt may', marketCapWeight: 3, percentChange: -0.4, moneyFlow: -15 },
];

const MONEY_FLOW_SECTOR_CODES = ['NH', 'CNTT', 'THEP', 'CK', 'BL', 'HC', 'XD', 'DK', 'BDS'];

export function getMarketIndices(): IndexQuote[] {
  return INDICES;
}

export function getIndexSparkline(symbol: string): number[] {
  return INDEX_SPARKLINES[symbol] ?? [];
}

export function getMarketBreadth(): MarketBreadthSnapshot {
  return BREADTH;
}

export function getSectorTreemap(): SectorSnapshot[] {
  return SECTORS;
}

export function getSectorMoneyFlow(): SectorSnapshot[] {
  return SECTORS.filter((sector) => MONEY_FLOW_SECTOR_CODES.includes(sector.code)).sort(
    (a, b) => b.moneyFlow - a.moneyFlow,
  );
}

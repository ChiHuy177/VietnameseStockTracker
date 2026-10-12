// Dữ liệu minh họa (mock) cho độ rộng thị trường + ngành — chưa có endpoint backend nào cho 2 phần
// này (xem CLAUDE.md, mục "Trang chủ kiểu Fireant", việc 2 và 3: cần sync ngành ICB + snapshot giá
// toàn thị trường). Các hàm accessor dưới đây trả đồng bộ, không phải Promise — khi nối API thật,
// phía gọi (component) cần đổi sang useQuery, không chỉ đổi nội dung hàm này.
//
// Phần chỉ số thị trường đã có backend thật và KHÔNG còn mock ở đây — xem
// `useMarketIndices`/`useIndexSparkline`.
import type { MarketBreadthSnapshot } from '@/models/marketBreadth';
import type { SectorSnapshot } from '@/models/sector';

const SNAPSHOT_TIME = '2026-10-11T03:32:15Z';

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

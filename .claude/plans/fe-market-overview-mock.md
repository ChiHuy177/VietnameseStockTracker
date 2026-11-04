# FE mock: trang chủ "tổng quan thị trường" (ticker/breadth/treemap/dòng tiền)

## Context

Research + 1 artifact mockup (đã được user duyệt — "oke hợp lí") cho thấy trang chủ hiện tại chỉ
có watchlist, thiếu hoàn toàn khái niệm "thị trường chung" (chỉ số, độ rộng, ngành, dòng tiền) mà
mọi trang stock VN thật (Fireant/SSI iBoard/CafeF) đều có. User đã chốt: build FE trước với mock
data cho 3 phần cần backend mới (chỉ số, treemap ngành, dòng tiền), ghi các việc BE cần làm vào
CLAUDE.md thay vì làm luôn. User cũng chốt áp dụng theme mới (navy/gold trading-terminal + font
Barlow Condensed/IBM Plex Mono) cho **toàn site**, không chỉ phần mới.

## 1. Theme & font

- `fe/src/index.css` — giữ nguyên cơ chế `:root` (light) / `.dark` (dark) đã có (
  `@custom-variant dark (&:is(.dark *))` tại line 3), sơn lại cả 2 block với palette mới (giấy ấm
  cho light, navy-đen cho dark; accent vàng-đồng `#c98a2e`/`#ad7322` thay primary xanh-indigo cũ).
  Thêm token semantic mới **tách biệt khỏi accent**: `--up/--down/--ceiling/--floor/--ref` + biến
  thể `-soft` (nền nhạt cho chip/badge), cho cả 2 block. Thêm các token này vào `@theme inline` để
  Tailwind sinh class `bg-up`/`text-up`/`bg-up-soft`...
- `fe/index.html` — thêm `class="dark"` vào `<html>` để site mặc định hiển thị theme tối (chưa có
  toggle UI nào tồn tại; đây là cách 1 dòng, dễ revert, không phải đổi tên/đảo ý nghĩa token).
- Font: dùng `@fontsource/barlow-condensed` + `@fontsource/ibm-plex-mono` (npm, tự host qua
  node_modules, đúng pattern "capability đến qua npm" đã dùng cho lucide-react/lightweight-charts —
  `fe/public/` hiện chỉ có `favicon.svg`, không có tiền lệ asset tĩnh thủ công). Import 4 file CSS
  weight cần dùng (600/700 Barlow, 500/600 Plex Mono) từ `fe/src/styles/fonts.css` (mới), import ở
  đầu `index.css`. Thêm `--font-display`/`--font-data` vào `@theme inline`.
- `fe/package.json` — thêm 2 dependency trên.

## 2. Models mới (`fe/src/models/`, theo đúng pattern `priceQuote.ts`)

- `marketIndex.ts` — `IndexQuote { symbol; name; value; change; percentChange; time }`
- `marketBreadth.ts` — `MarketBreadthSnapshot { advancing; declining; unchanged; ceiling; floor; time }`
- `sector.ts` — `SectorSnapshot { code; name; marketCapWeight; percentChange; moneyFlow }` (1 shape
  dùng chung cho cả treemap lẫn money-flow, khác nhau ở tập con dữ liệu)

## 3. Feature folder mới: `fe/src/features/marketOverview/`

Một folder duy nhất cho cả 4 widget (khác với các widget độc lập như watchlist/priceBoard, 4 cái
này luôn hiển thị cùng nhau, 2 trong số đó dùng chung mock data ngành) — tránh trùng lặp mock array
hoặc import chéo không cần thiết giữa các sibling không tồn tại độc lập.

- `marketOverviewMockData.ts` — hardcode `mockIndices`, `mockIndexSparklines`, `mockMarketBreadth`,
  `mockSectorTreemap` (~14 ngành), `mockSectorMoneyFlow` (~9 ngành) + accessor function đồng bộ
  (`getMarketIndices()` v.v.) — **không** async/TanStack Query ở bước này; ghi rõ trong file rằng
  nối backend thật sau này cần sửa phía gọi (thêm `useQuery`), không phải đổi 1 dòng.
- `treemapLayout.ts` — hàm thuần, không đụng DOM:
  `computeTreemapLayout<T extends {weight:number}>(items, containerWidth, containerHeight, numRows)
  → Array<{item, x, y, width, height}>`. Thuật toán "greedy rows": sort giảm dần theo weight, chia
  vào `numRows` hàng sao cho tổng weight mỗi hàng ~ bằng nhau, hàng cao theo tỷ trọng weight hàng đó
  so với tổng, cell trong hàng rộng theo tỷ trọng weight trong hàng. Đủ tốt cho mock ~14 item, không
  cần squarified treemap đầy đủ.
- `treemapLayout.test.ts` — unit test thuần (empty/single-item/even-weight/numRows>items/tiling
  invariant tổng width/height).
- `IndexTicker.tsx` — lưới 4 cột, mỗi ô: tên (font-display, uppercase) + giá trị (font-data) +
  delta màu `text-up`/`text-down` + `<Sparkline>` (component đã có sẵn ở
  `fe/src/components/ui/sparkline.tsx`, tái dùng nguyên, không viết chart mới).
- `MarketBreadthBar.tsx` — thanh 3 đoạn tỷ lệ (tăng/đứng giá/giảm) + legend số liệu + 2 chip
  trần/sàn dùng `bg-ceiling-soft`/`bg-floor-soft`.
- `SectorTreemap.tsx` — gọi `computeTreemapLayout` với không gian tọa độ logic cố định (ví dụ
  1000×300, không dùng ResizeObserver), render rect bằng `%` nên vẫn responsive; màu theo
  %change trên thang đỏ→trung tính→xanh (nội suy giữa token `--down`/`--up`).
- `SectorMoneyFlow.tsx` — bar chart 2 chiều quanh mốc 0, dương sang phải (`bg-up`), âm sang trái
  (`bg-down`), độ rộng theo tỷ lệ với giá trị tuyệt đối lớn nhất trong danh sách.
- Test co-located cho 4 component trên: `render` thuần (KHÔNG dùng `renderWithProviders` — các
  widget này không phụ thuộc Redux/Query/Router), assert text/role + class màu trên 1 case dương/1
  case âm biết trước; `SectorTreemap.test.tsx` chỉ smoke-test rendering (thuật toán đã có test riêng
  ở `treemapLayout.test.ts`).

## 4. Đồng bộ màu semantic ở component đã có (tránh 2 hệ màu song song)

- `fe/src/features/watchlist/WatchlistTable.tsx` — `text-green-600`/`stroke-green-500` →
  `text-up`/`stroke-up`; `text-red-600`/`stroke-red-500` → `text-down`/`stroke-down`.
- `fe/src/features/priceBoard/PriceBoardPanel.tsx` — các hằng màu trần/sàn/tham chiếu (hiện đang là
  `fuchsia`/`blue`/`yellow` rời rạc) → `text-ceiling`/`text-floor`/`text-ref` +
  `bg-ceiling-soft`/`bg-floor-soft`/`bg-ref-soft`; up/down → `text-up`/`text-down`.
- `fe/src/features/chart/ohlcvChartData.ts` — bỏ hằng `UP_COLOR`/`DOWN_COLOR` module-level, đổi
  `toVolumeData(bars, upColor, downColor)` nhận màu qua tham số (giữ hàm thuần/dễ test).
- `fe/src/features/chart/OhlcvChart.tsx` — đọc `--up`/`--down` một lần qua
  `getComputedStyle(document.documentElement)` lúc mount, dùng cho candle series + truyền xuống
  `toVolumeData`. Lý do phải đọc raw CSS variable ở đúng chỗ này: `lightweight-charts` vẽ ra
  `<canvas>`, không nhận class Tailwind như các component DOM khác.
- Cập nhật test liên quan: `ohlcvChartData.test.ts` (truyền màu literal qua tham số mới);
  `WatchlistTable.test.tsx`/`PriceBoardPanel.test.tsx` — đã kiểm tra, không assert literal class màu
  nên không cần sửa.

## 5. Wiring `HomePage.tsx`

Thêm theo đúng thứ tự đã duyệt trong mockup, nằm **trong** `HomePage.tsx` (không đụng
`App.tsx`/`max-w-7xl`): `<IndexTicker />` → `<MarketBreadthBar />` → hàng `grid lg:grid-cols-3` gồm
`<SectorTreemap className="lg:col-span-2" />` + `<SectorMoneyFlow />` → giữ nguyên khối search +
watchlist (`grid-cols-5`) hiện tại ở cuối, chỉ ăn theme màu mới qua token (không đổi cấu trúc).

## 6. Cập nhật CLAUDE.md (việc BE ghi vào plan, không làm luôn)

Bổ sung subsection mới trong mục "Trang chủ kiểu Fireant" (dòng 94-138 hiện tại) — **"FE mock UI đã
xong — hợp đồng field cho backend"** — liệt kê chính xác 3 shape ở mục 2 trên (`IndexQuote`,
`MarketBreadthSnapshot`, `SectorSnapshot`) làm hợp đồng cho BE khi làm thật, map rõ:
- `IndexQuote` ↔ việc `fetch_index()`/`GET /index` đã ghi ở item 1 (sparkline/history nên là
  endpoint OHLCV riêng, không nhúng inline, giống pattern `fetch_ohlcv` hiện có).
- `SectorSnapshot.code/name` ↔ cột `industry`/`icb_code` đã ghi ở item 2; `.moneyFlow` ↔
  field `total_value` từ `VnstockPriceBoardDto` đã có nhưng chưa map, ghi ở item 3.
- `MarketBreadthSnapshot` ↔ đánh đổi EOD-vs-realtime đã ghi ở item 3.
- Ghi rõ: các accessor mock (`getMarketIndices` v.v.) hiện là hàm đồng bộ trả hằng số; nối BE thật
  sau này là việc sửa phía gọi (bọc `useQuery`), không phải chỉ đổi 1 dòng.
- Không đổi phần "Không nằm trong việc này" — việc này chỉ thêm hợp đồng field FE, không bật đèn
  xanh làm BE.

## Verification

- `npm run test` (fe/) — toàn bộ test mới + test sửa đổi phải pass.
- `npm run dev` — mở `http://localhost:5173`, kiểm tra bằng mắt: ticker/breadth/treemap/dòng tiền
  hiện đúng dữ liệu mock, theme tối mặc định, watchlist/stock-detail vẫn hoạt động đúng (không vỡ
  layout, màu trần/sàn/tham chiếu trên `PriceBoardPanel` vẫn đúng quy ước sau khi đổi token).
- `npm run lint` + `npm run build` (tsc -b) — đảm bảo không có lỗi type từ model/token mới.

# CLAUDE.md

Hướng dẫn cho Claude Code khi làm việc trong repo này.

## Tổng quan

Xem [docs/PROGRESS.md](docs/PROGRESS.md) để biết đầy đủ kiến trúc, luồng dữ liệu và tiến độ từng
phase (0-4). File này chỉ dùng để ghi lại các việc **đang chờ làm** mà một phiên trước đã cố ý để
lại — tránh bị quên giữa các phiên làm việc.

## Việc đang chờ (backend) — mở rộng `PriceQuote` real-time cho trang chi tiết mã

**Bối cảnh**: FE đã build xong trang chi tiết mã (`fe/src/pages/StockDetailPage.tsx`) theo giao
diện kiểu SSI iBoard — quy ước màu chuẩn thị trường VN: **tím = giá trần**, **xanh dương = giá
sàn**, **vàng = giá tham chiếu**, cộng bảng giá mua/bán 3 mức và room khối ngoại
(`fe/src/features/priceBoard/PriceBoardPanel.tsx`). Quyết định lúc đó (đã hỏi user, đã chốt): làm
FE trước theo model đầy đủ, BE bổ sung sau — ghi note ở đây thay vì làm luôn để không đụng lại code
Phase 3 đã commit trong cùng 1 hơi.

Model FE `fe/src/models/priceQuote.ts` đã có sẵn các field mở rộng, khai báo **optional** — UI tự
hiển thị ("—" hoặc ẩn hẳn phần bid/ask) khi field chưa tồn tại. Nghĩa là **không cần đổi gì ở FE**
khi làm xong phần BE dưới đây — cứ bổ sung field, backend gửi lên, FE tự sáng.

Dữ liệu thô cần thiết **đã có sẵn** ở Python service (`GET /price-board`, xem
`python/src/vst_python/fetcher.py::fetch_price_board`, trả nguyên `Trading().price_board()` của
vnstock) — cột `reference_price`, `ceiling_price`, `floor_price`, `bid_price_1..3`, `bid_vol_1..3`,
`ask_price_1..3`, `ask_vol_1..3`, `foreign_buy_volume`, `foreign_sell_volume`, `foreign_room` đều đã
có trong response, chỉ là backend Java hiện chưa map. **Không cần sửa Python.**

### Việc cần làm (theo đúng pattern anti-corruption layer đã dùng xuyên suốt project)

1. **`be/src/main/java/com/vst/backend/dto/vnstock/VnstockPriceBoardDto.java`** — thêm field raw
   (dùng `@JsonProperty` cho snake_case), kiểu `BigDecimal` cho giá, `Long` cho khối lượng:
   `referencePrice`, `ceilingPrice`, `floorPrice`, `bidPrice1/2/3`, `bidVol1/2/3`, `askPrice1/2/3`,
   `askVol1/2/3`, `foreignBuyVolume`, `foreignSellVolume`, `foreignRoom`.

2. **`be/src/main/java/com/vst/backend/model/PriceQuote.java`** — thêm field domain tương ứng.
   Khác với 6 field gốc (`symbol/time/price/change/percentChange/volume`, đang required trong
   mapper), các field mới nên **nullable/optional** — vnstock không phải lúc nào cũng trả đủ (ví
   dụ ngoài giờ giao dịch, hoặc mã không có ai đặt lệnh mua/bán). Cân nhắc gom bid/ask thành
   `List<PriceLevel>` (record `PriceLevel(BigDecimal price, long volume)`) thay vì 6 field rời rạc
   `bidPrice1..3`/`askPrice1..3` — khớp với shape `bids`/`asks` mảng mà FE đã viết sẵn
   (`fe/src/models/priceQuote.ts`), và mapper là đúng chỗ để làm phép biến đổi flat → array này.

3. **`be/src/main/java/com/vst/backend/mapper/PriceQuoteMapper.java`** — map thêm field mới,
   **không** `requireField()`/throw cho field mới (khác 6 field gốc) — null thì cứ để null, domain
   model đã optional. Cập nhật `PriceQuoteMapperTest` theo (thêm case field mới null vẫn map được
   bình thường, không throw).

4. `PriceBoardPollerService`, `PriceBoardBroadcaster`, `PriceBoardUpdateEvent` — **không cần đổi
   gì**, chỉ đi theo `PriceQuote` đã mở rộng (chúng chỉ cầm `List<PriceQuote>`, không biết field
   bên trong).

5. Verify: add 1 mã vào watchlist, chờ 1 chu kỳ poll (3s), mở `/stocks/{symbol}` trên FE — panel
   trần/sàn/tham chiếu + bảng mua/bán phải tự hiện ra (hiện tại đang hiện "—"/ẩn vì thiếu field).

### Không nằm trong việc này

- `price_snapshots` hypertable (đã tạo từ Phase 1, chưa dùng) — việc persist lịch sử tick real-time
  là việc khác, chưa có yêu cầu.
- Không cần đổi `VnstockClient.fetchPriceBoard()` hay endpoint `/price-board` — chỉ đổi tầng
  DTO/model/mapper.

## Việc đang chờ (kiến trúc) — Multi-user / auth cho watchlist

**Bối cảnh**: watchlist hiện là **global, dùng chung cho mọi người** — quyết định cố ý từ Phase 2
(đã hỏi user, đã chốt: "Global, không cần auth" để tránh xây hệ thống user/login ở giai đoạn học).
`WatchlistController`/`WatchlistService`/`WatchlistRepository` đều có comment ghi rõ điều này.

**Vấn đề nếu có nhiều người dùng thật cùng lúc**: vì watchlist không tách theo người dùng, user A
thêm/xóa mã sẽ ảnh hưởng ngay tới màn hình user B đang xem — không phải bug, mà là **sai bản chất
thiết kế dữ liệu** một khi có nhiều người dùng thật. Không phải vấn đề hiệu năng, sửa performance
không giải quyết được.

### Việc cần làm khi tới lúc (chưa làm gì, chỉ ghi nhớ hướng đi)

1. Cần cơ chế xác thực người dùng (login) — project hiện **hoàn toàn chưa có** khái niệm user/auth
   ở bất kỳ đâu (xem `AppException` hierarchy — chưa có `UnauthorizedException`/`ForbiddenException`).
2. Migration mới: thêm bảng `users`, thêm cột `user_id` vào `watchlist_items`
   (`be/src/main/resources/db/migration/V4__watchlist.sql` hiện không có cột này).
3. `WatchlistRepository`/`WatchlistService`/`WatchlistController` — mọi query phải lọc/ghi theo
   `user_id` của người đang đăng nhập, không phải lấy toàn bộ bảng như hiện tại.
4. Cân nhắc: `PriceBoardPollerService` hiện poll theo watchlist global — nếu watchlist theo từng
   user, cần quyết định lại phạm vi poll (hợp toàn bộ symbol của mọi user? poll riêng theo từng
   user đang online? ảnh hưởng tới thiết kế WebSocket/STOMP hiện tại — mọi client đang nhận chung 1
   broadcast `/topic/price-board`, không phân biệt user).

### Không nằm trong việc này (chưa quyết định, cần hỏi lại khi tới lúc)

- Chưa chọn cơ chế auth cụ thể (JWT? session? OAuth?) — chưa có quyết định, không tự ý chọn khi
  làm việc này.
- Chưa rõ có cần phân quyền (roles) hay chỉ cần "đăng nhập là đủ" — cần hỏi lại user.

## Việc đang chờ (tính năng + backend) — Trang chủ kiểu Fireant: chỉ số thị trường, treemap ngành, dòng tiền

**Bối cảnh**: user gửi ảnh chụp trang "Thị trường" của Fireant làm tham khảo redesign trang chủ.
Phần khả thi ngay (watchlist dạng list-card + sparkline) đã làm. Mục 1 (chỉ số thị trường) **đã làm
xong** cả BE lẫn FE. Mục 2 và 3 dưới đây vẫn **cố ý chưa làm** vì đòi hỏi nguồn dữ liệu hoàn toàn
mới, không phải việc UI đơn thuần — đã trình bày rõ với user và được đồng ý hoãn lại.

### 1. Chỉ số thị trường (VNINDEX / HNXINDEX / UPCOMINDEX / VN30) — ĐÃ XONG

Làm theo `.claude/plans/be-market-index.md` (có ghi lại phần research vnstock). Tóm tắt những gì
đang chạy, để không ai làm lại:

- `fetch_index_ohlcv` + `GET /index-ohlcv` ở Python (`_market.index(symbol).ohlcv(...)`), trả cùng
  cột với `/ohlcv` nên **tái dùng `VnstockOhlcvDto`/`OhlcvBar`/`OhlcvMapper` nguyên xi**, không có
  DTO/mapper mới.
- `V6__market_indices.sql` — cột `stocks.kind ('STOCK'|'INDEX')` + seed 4 chỉ số; tái dùng
  `price_history_ohlcv` thay vì bảng riêng. `StockRepository.findAllSymbols()` đã lọc
  `kind='STOCK'` để full-catalog ingestion không fetch index như mã cổ phiếu.
- `IndexIngestionService` (cron `0 10 18 * * MON-FRI`), `IndexQuoteService`,
  `GET /api/v1/indices` + `GET /api/v1/indices/{symbol}/ohlcv`.
- FE đã nối API thật: `services/marketIndexService.ts` + `useMarketIndices`/`useIndexSparkline`,
  `IndexTicker.tsx` không còn dùng mock (mock chỉ số đã bị xóa khỏi `marketOverviewMockData.ts`).

**Việc còn chờ ở mục này** (nhỏ, nằm ở BE — chưa làm, đã nói với user): `IndexQuoteService.toQuote`
throw `ResourceNotFoundException` khi **một** chỉ số chưa có bar nào, mà vì nằm trong `.map()` nên
làm **cả list** `GET /api/v1/indices` trả 404. Trên DB mới (chưa qua phiên ingest 18:10 nào) endpoint
coi như chết — FE hiện hiển thị dòng gợi ý "job ingest chỉ số chưa chạy phiên nào" thay vì báo lỗi
đỏ. Hướng sửa: bỏ qua chỉ số chưa có dữ liệu thay vì throw (chỉ 404 khi **không** chỉ số nào có
dữ liệu), nhớ cập nhật `IndexQuoteServiceTest`. Cũng chưa có cách trigger ingest bằng tay ngoài cron
— đang phải chờ cron hoặc gọi `ingestAll()` thủ công.

### 2. Treemap theo ngành (Tài chính, Bất động sản, Vật liệu cơ bản...)

Bảng `stocks` hiện chỉ có `symbol/name/exchange` — **không có phân loại ngành**. vnstock có
`Listing().industries_icb()` và `Listing().symbols_by_industries()` (đã liệt kê lúc khảo sát thư
viện, xem lịch sử chat/docs/PROGRESS.md) — chưa từng gọi hay wiring.
1. Thêm cột `industry`/`icb_code` vào bảng `stocks` (migration mới).
2. Mở rộng `StockListingSyncService` (job sync định kỳ đã có) để lấy thêm ngành, không chỉ
   `name`/`exchange` như hiện tại.
3. Treemap cần % thay đổi giá theo ngành — phụ thuộc mục 3 dưới đây (breadth toàn thị trường).

### 3. Số mã Tăng/Giảm/Không đổi + phân bố dòng tiền toàn thị trường

Cần giá real-time (hoặc ít nhất giá cuối ngày) cho **toàn bộ ~1500 mã**, không chỉ mã trong
watchlist. Đây là thay đổi phạm vi lớn so với quyết định Phase 3 đã chốt (real-time cố ý giới hạn
watchlist để tránh vượt rate-limit vnstock — xem phần Phase 3 trong `docs/PROGRESS.md`). Cần bàn
lại đánh đổi: có thể dùng dữ liệu **cuối ngày** (đọc từ `price_history_ohlcv` đã ingest, không cần
real-time) cho phần "biến động thị trường" này thay vì đòi real-time toàn thị trường — rẻ hơn nhiều
về mặt gọi API, hợp lý hơn cho quy mô project. "Dòng tiền" (`total_value` — vnstock đã trả field
này trong `price-board`, xem `VnstockPriceBoardDto`) là khái niệm riêng, chưa map ở backend.

### FE mock UI đã xong — hợp đồng field cho backend

User đã duyệt 1 bản mockup thiết kế (theme trading-terminal tối + font Barlow Condensed/IBM Plex
Mono) và chốt: làm FE trước với **mock data cứng** cho cả 3 phần trên, BE làm sau — xem
[fe/src/features/marketOverview/](fe/src/features/marketOverview/). Đây **không phải** quyết định
bật đèn xanh làm BE, chỉ là ghi lại hợp đồng field để khi làm BE thật thì khớp đúng shape FE đã
dựng, khỏi phải đổi FE lần 2:

- ~~`IndexTicker.tsx`~~ — **đã nối BE thật**, không còn là hợp đồng chờ: `IndexQuote` khớp
  field-by-field với `IndexQuoteResponse`, sparkline lấy từ endpoint OHLCV riêng
  (`GET /api/v1/indices/{symbol}/ohlcv`) đúng như hợp đồng ban đầu, không nhúng inline trong
  `IndexQuote`. Xem mục 1.
- `SectorTreemap.tsx`/`SectorMoneyFlow.tsx` dùng chung `SectorSnapshot { code; name;
  marketCapWeight; percentChange; moneyFlow }` (`fe/src/models/sector.ts`) ↔ mục 2 và 3 —
  `code`/`name` map tới cột `industry`/`icb_code` sẽ thêm ở mục 2; `moneyFlow` map tới field
  `total_value` đã có sẵn trong `VnstockPriceBoardDto` nhưng chưa map, nói ở mục 3.
- `MarketBreadthBar.tsx` dùng `MarketBreadthSnapshot { advancing; declining; unchanged; ceiling;
  floor; time }` (`fe/src/models/marketBreadth.ts`) ↔ mục 3 — đánh đổi EOD-vs-realtime đã ghi ở
  trên áp dụng trực tiếp cho field này.
- Các hàm mock còn lại (`getMarketBreadth`, `getSectorTreemap`, `getSectorMoneyFlow` ở
  `fe/src/features/marketOverview/marketOverviewMockData.ts`) vẫn là hàm **đồng bộ** trả hằng số —
  nối BE thật sau này là việc sửa phía gọi (bọc `useQuery`), không phải chỉ đổi nội dung 1 file.
  Mẫu để copy khi tới lúc: `useMarketIndices`/`useIndexSparkline` + `marketIndexService.ts` của mục 1.
- Màu semantic trần/sàn/tham chiếu/tăng/giảm đã được gom thành token CSS (`--up/--down/--ceiling/
  --floor/--ref` trong `fe/src/index.css`) thay vì hardcode rời rạc như trước — nếu sau này thêm
  UI mới liên quan tới các khái niệm này, dùng lại token, không tạo màu hardcode mới.

### Không nằm trong việc này

- Mục 2 (ngành ICB) và mục 3 (breadth + dòng tiền): chưa quyết định có làm BE hay không, chỉ ghi
  lại hướng đi + hợp đồng field FE nếu sau này quyết định làm. Phần research đã xong cho 2 mục này
  nằm ở cuối `.claude/plans/be-market-index.md` (ICB phải dùng `Listing(source="vci")`; breadth cần
  `INGESTION_ALL_SYMBOLS=true` + chờ ≥2 phiên; treemap không có market cap thật nên phải dùng
  turnover, `moneyFlow` chỉ là heuristic).
- Watchlist list-card + sparkline (phần khả thi) đã làm xong, không thuộc phạm vi ghi chú này.

# Backend: ingest toàn thị trường + API chỉ số (VNINDEX/VN30/HNX-INDEX/UPCOM-INDEX)

## Context

FE mock cho "market overview" (ticker chỉ số, breadth, treemap ngành, dòng tiền) đã xong, dùng mock
data ở `fe/src/features/marketOverview/`. User chọn làm backend thật cho phần **chỉ số** trước
(nhỏ, không rủi ro vận hành) — breadth/treemap ngành/dòng tiền (cần bật ingest toàn thị trường +
sync ICB + snapshot giá hàng ngày) để làm phiên sau, ghi chú lại dưới mục "Còn lại" cuối file.

Research đã xác nhận (đọc trực tiếp source `vnstock==4.0.9` đã cài, không đoán):
- `Market().index("VNINDEX").ohlcv(start=, end=)` là API thật, **giống hệt hình dạng dữ liệu** với
  `_market.equity(symbol).ohlcv(...)` đang dùng (`time, open, high, low, close, volume`). Symbol
  literal: `"VNINDEX"`, `"HNXINDEX"`, `"UPCOMINDEX"`, `"VN30"` (không gạch ngang).
- **Không có API lấy giá trị chỉ số hiện tại** (live snapshot) — `Trading.price_board()` không có
  nhánh xử lý cho index, và gọi intraday cho index sẽ raise lỗi ở cả KBS/VCI. Giá trị "hiện tại" +
  change/%change phải tính từ **2 bar OHLCV gần nhất** (today close vs. previous close) — một phép
  tính hoàn toàn mới, project hiện chưa có đâu làm việc này cho cả mã cổ phiếu.
- Vì shape dữ liệu giống hệt OHLCV mã cổ phiếu, **tái dùng `price_history_ohlcv` + `stocks`** (thêm
  cột đánh dấu index) rẻ hơn nhiều so với bảng riêng — mọi DTO/model/mapper/repository OHLCV hiện có
  dùng lại nguyên, không sửa.

## Việc cần làm

### 1. Migration — đánh dấu index trong `stocks`, seed 4 dòng

`be/src/main/resources/db/migration/V5__market_indices.sql` (mới):
```sql
ALTER TABLE stocks
    ADD COLUMN kind VARCHAR(10) NOT NULL DEFAULT 'STOCK' CHECK (kind IN ('STOCK', 'INDEX'));

INSERT INTO stocks (symbol, name, exchange, kind) VALUES
    ('VNINDEX', 'VN-INDEX', 'HOSE', 'INDEX'),
    ('VN30', 'VN30', 'HOSE', 'INDEX'),
    ('HNXINDEX', 'HNX-INDEX', 'HNX', 'INDEX'),
    ('UPCOMINDEX', 'UPCOM-INDEX', 'UPCOM', 'INDEX')
ON CONFLICT (symbol) DO UPDATE SET kind = EXCLUDED.kind, name = EXCLUDED.name;
```

`StockRepository.java` — sửa `findAllSymbols()` thêm `WHERE kind = 'STOCK'` (quan trọng: nếu không,
`OhlcvIngestionService` khi chạy với `all-symbols=true` sẽ gọi `fetchOhlcv("VNINDEX", ...)` — API
mã cổ phiếu không hiểu symbol index, sẽ lỗi hoặc trả rác); thêm `findIndexSymbols()` (`WHERE kind =
'INDEX'`) cho job ingest chỉ số mới.

**Không bật `INGESTION_ALL_SYMBOLS=true`** trong phần việc này — đó là việc của phần breadth/sector
(phiên sau), không cần cho riêng tính năng chỉ số.

### 2. Python — `fetch_index_ohlcv` + route mới

`python/src/vst_python/fetcher.py` — thêm, cạnh `fetch_ohlcv` đã có:
```python
def fetch_index_ohlcv(symbol: str, start: str, end: str):
    return _market.index(symbol).ohlcv(start=start, end=end)
```
`python/src/vst_python/api.py` — thêm `GET /index-ohlcv?symbol=&start=&end=`, giống hệt khung
try/except của `get_ohlcv` hiện có. Trả về cùng cột `time,open,high,low,close,volume` → **không cần
DTO Java mới**, tái dùng `VnstockOhlcvDto` nguyên.

### 3. Java — ingest + tính "giá trị hiện tại" từ 2 bar gần nhất

- `VnstockClient.java` — thêm `fetchIndexOhlcv(symbol, start, end)`, copy nguyên cấu trúc
  `fetchOhlcv` (cùng `@Retry(name="vnstock") @CircuitBreaker(name="vnstock")`), chỉ đổi path thành
  `/index-ohlcv`.
- `PriceHistoryRepository.java` — thêm `findLatestTwoBars(long stockId)` (`ORDER BY time DESC LIMIT
  2`).
- `service/IndexIngestionService.java` (mới) — sibling của `OhlcvIngestionService`: `@Scheduled(cron
  = "${index-ingestion.cron}")`, loop `stockRepository.findIndexSymbols()`, gọi
  `vnstockClient.fetchIndexOhlcv(...)`, map bằng **`OhlcvMapper.toDomain` hiện có, không sửa gì**,
  upsert bằng **`PriceHistoryRepository.upsertAll` hiện có**. Per-symbol try/catch như
  `ingestOne`.
- `service/IndexQuoteService.java` (mới) — `getCurrentQuotes()`: với mỗi index, lấy 2 bar gần
  nhất, `change = latest.close - previous.close`, `percentChange = change/previous.close*100`. Edge
  case: 0 bar → `ResourceNotFoundException`; đúng 1 bar (ngày đầu sau khi deploy) → `change=0,
  percentChange=0`, không throw.
- `dto/IndexQuoteResponse.java` (mới) — record khớp field-by-field với FE model `IndexQuote`:
  `(symbol, name, value, change, percentChange, time)`.
- `controller/IndexController.java` (mới, **không** nhúng vào `StockController` — index không phải
  mã cổ phiếu, không search/watchlist được):
  - `GET /api/v1/indices` → `List<IndexQuoteResponse>`.
  - `GET /api/v1/indices/{symbol}/ohlcv?start=&end=` → route thẳng tới `OhlcvQueryService.getHistory()`
    hiện có (nó tra theo symbol qua `stockRepository.findBySymbol`, không quan tâm `kind`) — không
    cần code mới, chỉ cần route, dùng để backing sparkline chỉ số ở FE sau này.

### 4. Config (`application.yml`)

```yaml
index-ingestion:
  symbols: ${INDEX_INGESTION_SYMBOLS:VNINDEX,VN30,HNXINDEX,UPCOMINDEX}
  lookback-days: ${INDEX_INGESTION_LOOKBACK_DAYS:7}
  cron: ${INDEX_INGESTION_CRON:0 10 18 * * MON-FRI}   # lệch 10 phút sau ingestion.cron, cùng vnstock service nên tránh chồng chéo
```

### 5. Test (theo đúng convention đã có trong repo)

- `IndexQuoteServiceTest` — Mockito, mock `PriceHistoryRepository`, fixture 2 bar → assert
  change/percentChange đúng; fixture 1 bar → change=0; fixture 0 bar → `ResourceNotFoundException`.
  (mirror `OhlcvQueryServiceTest.java`)
- `IndexControllerTest` — `@WebMvcTest(IndexController.class)` + `@MockBean IndexQuoteService` +
  `MockMvc` + `jsonPath`. (mirror `StockControllerTest.java`/`WatchlistControllerTest.java`)
- Không cần test riêng cho `IndexIngestionService`/`VnstockClient.fetchIndexOhlcv` — logic tái dùng
  100% `OhlcvMapper`/`PriceHistoryRepository` đã có test; chỉ cần đọc lại test hiện có của chúng để
  chắc chắn không phá.

## Verification

1. Chạy migration (Flyway tự áp dụng khi start backend) — kiểm tra `SELECT symbol, kind FROM stocks
   WHERE kind='INDEX'` trả 4 dòng.
2. Chạy Python service (port 8001 như đang chạy) + test thủ công
   `curl "http://localhost:8001/index-ohlcv?symbol=VNINDEX&start=2026-09-01&end=2026-10-11"` trả
   mảng bar.
3. Chạy backend, trigger `IndexIngestionService.ingestAll()` thủ công (hoặc chờ cron, hoặc expose
   tạm 1 endpoint debug) — kiểm tra `price_history_ohlcv` có bar cho `stock_id` của VNINDEX/VN30/...
4. `curl http://localhost:8080/api/v1/indices` → trả 4 object, field khớp `IndexQuote` ở FE
   (`fe/src/models/marketIndex.ts`).
5. `curl http://localhost:8080/api/v1/indices/VNINDEX/ohlcv?start=...&end=...` → trả bars.
6. `./mvnw test` (be/) — toàn bộ test cũ + mới phải pass.
7. (Chưa làm ở bước này) Nối FE — đổi `getMarketIndices()`/`getIndexSparkline()` trong
   `fe/src/features/marketOverview/marketOverviewMockData.ts` từ mock sang gọi API thật qua
   `useQuery` — để phiên riêng, không làm trong phần này.

## Còn lại (chưa làm, ghi chú cho phiên sau — cập nhật CLAUDE.md khi làm)

Research đã xong, chỉ chưa triển khai (xem transcript phiên này nếu cần chi tiết lại):
- **Phân loại ngành ICB**: `Listing()` mặc định của project (KBS) **không hỗ trợ ICB**
  (`industries_icb()` raise `NotImplementedError`) — phải dùng `Listing(source="vci")` riêng.
  Thêm cột `stocks.icb_code`/`industry_name`, service sync mới (không gộp vào
  `StockListingSyncService` vì khác source).
- **Market breadth (tăng/giảm/đứng giá)**: cần bật `INGESTION_ALL_SYMBOLS=true` (tác động vận hành
  thật — cron `ingestion.cron` sẽ gọi ~1500 lần/ngày) và **chờ ≥2 phiên giao dịch** mới có dữ liệu
  so sánh. Tính qua `LAG(close) OVER (PARTITION BY stock_id ORDER BY time)` trên
  `price_history_ohlcv`.
- **Trần/sàn + dòng tiền ngành**: cần mở rộng `VnstockPriceBoardDto`/`PriceQuote` thêm
  `total_value/ceiling_price/floor_price/reference_price` (**trùng việc đã ghi ở CLAUDE.md mục đầu
  tiên** — nên làm chung 1 lần) + 1 job snapshot giá **toàn thị trường 1 lần/ngày sau giờ đóng cửa**
  (không phải poll 3s như watchlist — sẽ vi phạm rate limit). Cần bảng mới `daily_market_snapshot`
  (không tái dùng `price_snapshots` — bảng đó có nhịp ghi khác, Phase 1 để dành việc khác).
  **Lưu ý ngữ nghĩa**: không có dữ liệu market cap thật (vnstock không trả shares outstanding ở các
  API đã khảo sát) → treemap sẽ dùng **turnover (giá trị khớp lệnh) thay cho market cap**; "dòng
  tiền" `moneyFlow` cũng chỉ là heuristic `turnover × dấu(%change)`, không phải order-flow thật —
  cần ghi rõ trong code/CLAUDE.md, không ngụy trang là dữ liệu chính xác.

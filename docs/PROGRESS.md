# Tiến độ dự án

Tài liệu này tổng hợp những gì đã làm từ đầu dự án đến hiện tại, để bất kỳ ai (kể cả phiên làm
việc sau của Claude) đọc vào là nắm được bức tranh toàn cảnh mà không cần lục lại lịch sử chat.
Cập nhật mỗi khi đóng xong 1 phase/bước quan trọng.

## Tổng quan kiến trúc

```
be/       Backend API — Spring Boot 3.3.4, Java 21, Maven, 3-tier (controller/service/repository)
fe/       Frontend — React + TypeScript (strict) + Vite. Trang chủ (search + watchlist + giá real-time).
python/   Data microservice — FastAPI, wrap thư viện vnstock (data source Việt Nam)
```

Backend nói chuyện với `vnstock` **gián tiếp** qua Python service (HTTP nội bộ) — không bao giờ
gọi thẳng thư viện Python từ Java. Mọi dữ liệu từ vnstock đi qua 1 lớp **anti-corruption**
(`dto/vnstock/*` → `mapper/*` → `model/*`) trước khi vào domain/DB, để nếu vnstock đổi shape JSON
thì chỉ phải sửa đúng 1 mapper.

DB: TimescaleDB (Postgres + extension time-series), migration bằng Flyway
(`be/src/main/resources/db/migration/`).

## Phase 0 — Nền tảng (hoàn thành)

Scaffold tối thiểu để mỗi service khởi động được và nói chuyện được với nhau.

- **Backend**: Spring Boot 3-tier skeleton, `GlobalExceptionHandler` trả lỗi theo RFC 7807
  (`ProblemDetail` + `errorCode` + `timestamp`), hierarchy exception
  (`AppException` → `ValidationException`/`ResourceNotFoundException`/`ConflictException`/`DataFetchException`),
  `GET /api/v1/health`.
- **Docker Compose + TimescaleDB**: `docker-compose.yml`, migration `V1__init.sql`.
- **Frontend**: Vite + React + TS strict, cấu trúc `app/` `models/` `services/` `features/`,
  feature `health/` gọi API qua `httpClient` (axios) chứng minh FE↔BE wiring.
- **Python service scaffold**: `pyproject.toml`, package `vst_python` (chưa có logic).
- **CI**: `.github/workflows/ci.yml` — 3 job song song (`backend` mvnw verify, `frontend` lint+build,
  `python` import thử).

## Phase 1 — Ingest dữ liệu OHLCV (hoàn thành)

Nạp dữ liệu giá lịch sử (nến ngày) từ vnstock vào DB theo lịch, chạy nền — không có endpoint
public nào cho client ở phase này.

```
vnstock (Market.equity().ohlcv())
  → python/ FastAPI GET /ohlcv
  → be/ VnstockClient.fetchOhlcv()  [Resilience4j @Retry + @CircuitBreaker]
  → OhlcvMapper.toDomain()          [anti-corruption: validate, VN time → UTC Instant]
  → OhlcvIngestionService.ingestAll()  [@Scheduled(cron), 1 mã lỗi không chặn mã khác]
  → PriceHistoryRepository.upsertAll()
  → TimescaleDB hypertable price_history_ohlcv
```

- Migration `V2__price_data.sql`: hypertable `price_history_ohlcv` + `price_snapshots`
  (`price_snapshots` tạo sẵn nhưng **chưa dùng** — dự phòng cho dữ liệu snapshot real-time).
- Cron mặc định `INGESTION_CRON=0 0 18 * * MON-FRI` (18h các ngày trong tuần, sau giờ đóng cửa),
  danh sách mã cấu hình qua `INGESTION_SYMBOLS` (CSV).
- Load-test thực tế trong lúc verify e2e đã phát hiện và fix 2 bug thật (`8c17077`).

## Phase 2 — Backend API (hoàn thành, 4/4 bước)

Xây API layer để client đọc dữ liệu Phase 1 đã nạp, cộng thêm search và watchlist.

| Bước | Nội dung |
|---|---|
| 1 | OpenAPI/Swagger UI (`springdoc-openapi` 2.6.0) tại `/swagger-ui.html` |
| 2 | `GET /api/v1/stocks/{symbol}/ohlcv?start=&end=` — đọc lịch sử giá từ DB |
| 3 | `GET /api/v1/stocks?q=` — search theo mã/tên, backed bởi job sync định kỳ toàn bộ listing vnstock (`StockListingSyncService`, `Listing().symbols_by_exchange()`) vào cột `stocks.name`/`stocks.exchange` (migration `V3`), tránh gọi vnstock live mỗi lần search |
| 4 | Watchlist CRUD (`GET/POST /api/v1/watchlist`, `DELETE /api/v1/watchlist/{symbol}`) — **global, không có auth** (quyết định đã chốt, project chưa có khái niệm user), migration `V4` |

Watchlist add idempotent (upsert, không lỗi khi add trùng); remove phân biệt 2 loại 404 (mã chưa
từng tồn tại vs. mã tồn tại nhưng không có trong watchlist).

## Phase 3 — Real-time (backend xong, FE tiêu thụ ở Phase 4)

Mục tiêu: hiển thị giá gần real-time trên FE (kiểu app SSI), theo quyết định gốc đã chốt **không
dùng Kafka/Redis** — dùng Spring messaging + WebSocket trực tiếp.

```
watchlist (global) → PriceBoardPollerService [@Scheduled mỗi 3s]
  → VnstockClient.fetchPriceBoard(symbols)   [1 call cho cả batch, không loop từng mã]
  → python/ FastAPI GET /price-board          [Trading().price_board(symbols)]
  → PriceQuoteMapper.toDomain()                [anti-corruption]
  → ApplicationEventPublisher.publishEvent(PriceBoardUpdateEvent)
  → PriceBoardBroadcaster.onPriceBoardUpdate()  [@EventListener]
  → SimpMessagingTemplate.convertAndSend("/topic/price-board", quotes)
  → mọi client STOMP đã subscribe /topic/price-board nhận được
```

- **Phạm vi cố ý giới hạn ở watchlist** (không phải toàn bộ ~1500 mã) — quyết định đã hỏi và chốt
  với user, để tránh gọi vnstock quá nhiều/rủi ro rate-limit.
- **Poll interval 3s** = 20 request/phút tới vnstock — có margin so với quota Community tier
  (60 req/phút, cần `VNSTOCK_API_KEY` trong `.env`); tính cả OHLCV ingestion job dùng chung key.
  Guest tier (không có key) chỉ 20 req/phút — sát margin nếu rơi vào tier này, nên luôn set
  `VNSTOCK_API_KEY`.
- **STOMP over WebSocket** (đổi từ raw `TextWebSocketHandler` ban đầu, để FE dùng được
  `@stomp/stompjs` theo đúng stack Phase 4 đã chọn): endpoint bắt tay `/ws`, broker đơn giản
  in-memory (`@EnableWebSocketMessageBroker`, `enableSimpleBroker("/topic")`), không SockJS.
  Server-push only, không có destination nào cho client SEND.
- Unit test: `PriceQuoteMapperTest` (map field, reject null), `PriceBoardPollerServiceTest`
  (watchlist rỗng → không gọi gì; có mã → fetch/map/publish; lỗi fetch → không propagate).
- **`PriceQuote` đã mở rộng** (Phase 4): ngoài 6 field lõi còn trần/sàn/tham chiếu, 3 mức mua +
  3 mức bán (`List<PriceLevel>`, mapper gom từ field phẳng `bid_price_1..3` của vnstock), khối
  ngoại mua/bán/room, và `totalValue` (chưa ai đọc — dành cho "dòng tiền theo ngành"). Khác 6 field
  lõi, các field này **không** required: vnstock trả thiếu là bình thường (ngoài giờ giao dịch, mã
  không ai đặt lệnh) nên mapper để null, và `PriceQuote` khai `@JsonInclude(NON_NULL)` để field
  thiếu **vắng mặt** trong JSON — FE phân biệt "chưa có" bằng `undefined`, gửi null lên sẽ làm FE
  format một giá trị không tồn tại.

**Còn thiếu**: `price_snapshots` hypertable (tạo từ Phase 1) vẫn chưa được dùng để lưu lại lịch sử
tick real-time — hiện tại data chỉ broadcast, không persist.

## Phase 4 — Frontend (đang làm)

**Tooling scaffold**: ESLint (thay `oxlint`, flat config), TanStack Query (`QueryClientProvider`),
Redux Toolkit (`store.ts`), React Router (`BrowserRouter`), `@stomp/stompjs`, Tailwind v4 + shadcn/ui
(viết tay theo chuẩn `new-york` — CLI `npx shadcn` bị treo/crash trong môi trường monorepo này),
`lightweight-charts` (cài, chưa dùng), Vitest + React Testing Library.

**Trang `/` (HomePage)**:
- `SearchBox` — debounce 300ms, gọi `GET /api/v1/stocks?q=` qua TanStack Query, nút "+ Watchlist"
  trên mỗi kết quả (mutation, invalidate query watchlist khi thành công)
- `WatchlistTable` — `GET /api/v1/watchlist` qua TanStack Query, nút "Xóa" (mutation), cột **Giá**
  đọc real-time từ Redux (`priceBoardSlice`) — `useStompPriceBoard()` (chạy 1 lần ở `App.tsx`)
  subscribe `/topic/price-board`, dispatch quote mới nhất theo symbol vào store
- `IndexTicker` (`features/marketOverview/`) — 4 chỉ số VNINDEX/VN30/HNXINDEX/UPCOMINDEX từ
  `GET /api/v1/indices` qua TanStack Query (`useMarketIndices`), sparkline 30 ngày từ
  `GET /api/v1/indices/{symbol}/ohlcv` (`useIndexSparkline`)
- `MarketBreadthBar`/`SectorTreemap`/`SectorMoneyFlow` — **đã nối backend thật** (xem mục "Phase 4b
  — Breadth thị trường + treemap/dòng tiền ngành" bên dưới), không còn mock
  (`marketOverviewMockData.ts` đã xoá)
- `HealthCheck` (Phase 0) không còn render trong `App.tsx` nữa (route `/` đã có nội dung thật để
  chứng minh FE↔BE), nhưng file feature vẫn giữ lại

**Trang `/stocks/{symbol}` (StockDetailPage, lazy-load)**: chart OHLCV
(`lightweight-charts`) + `PriceBoardPanel` kiểu SSI iBoard (trần/sàn/tham chiếu + bảng mua/bán 3
mức + room ngoại) — đọc real-time từ Redux như watchlist, và **đã có dữ liệu thật** từ khi
`PriceQuote` được mở rộng (xem Phase 3). Phần nào vnstock không trả thì tự hiện "—"/ẩn, không lỗi.

**Theme**: trading-terminal tối (navy/gold) + font Barlow Condensed/IBM Plex Mono, màu semantic
trần/sàn/tham chiếu/tăng/giảm là token CSS (`--up/--down/--ceiling/--floor/--ref` trong `index.css`).

**Docker**: cả 4 service (`db`/`python`/`be`/`fe`) chạy được bằng `docker compose up` — FE build ra
static rồi serve qua nginx, nginx proxy `/api/` và `/ws` (có Upgrade header) sang backend.

## Phase 4b — Breadth thị trường + treemap/dòng tiền theo ngành (hoàn thành)

Phần còn lại của trang chủ kiểu Fireant (mục 2, 3 trong research cũ) — độ rộng thị trường
(tăng/giảm/đứng giá/trần/sàn) và treemap/dòng tiền theo ngành ICB. Research ban đầu
(`.claude/plans/be-market-index.md`) dự đoán cần tính breadth từ 2 bar OHLCV gần nhất và lo ngại chi
phí gọi vnstock (~1500 lần/ngày) — khi bắt tay code thì phát hiện rẻ hơn hẳn:

- `Trading().price_board(symbols)` chấp nhận **toàn bộ ~1500 mã trong 1 lần gọi** (verify thủ công:
  ~4s, không cần chia lô) và đã trả sẵn `price_change`/`percent_change`/`total_value` — không cần
  suy ra từ OHLCV.
- `Listing(source="vci").symbols_by_industries()` trả ICB cho toàn bộ mã trong **1 lần gọi HTTP**
  (không loop theo mã).

→ Cả 2 job mới chỉ tốn đúng 1 request vnstock/lần chạy, rẻ như `StockListingSyncService`.

```
stocks (toàn bộ ~1500 mã STOCK) → MarketSnapshotIngestionService [@Scheduled 18:20 T2-T6]
  → VnstockClient.fetchPriceBoard(symbols)   [1 call cho CẢ TOÀN THỊ TRƯỜNG, không loop]
  → PriceQuoteMapper.toDomain()               [tái dùng nguyên, per-row error isolation]
  → lọc bỏ mã close_price=0 (không khớp lệnh phiên đó — ~45% thị trường, nếu giữ lại sẽ làm
    sai lệch nặng số liệu "đứng giá")
  → MarketSnapshotRepository.upsertAll()       [bảng thường daily_market_snapshot, không hypertable]

Listing(source="vci") → IndustrySyncService [@Scheduled 2:30 hằng ngày]
  → lọc icb_level=2 (~19 ngành, vd "Ngân hàng", "Bất động sản")
  → StockRepository.upsertIndustries()         [update-only, cột stocks.icb_code/icb_name]
```

- `GET /api/v1/market/breadth` — đếm advancing/declining/unchanged/ceiling/floor trực tiếp bằng SQL
  (`count(*) FILTER (WHERE ...)`), đọc ngày snapshot mới nhất.
- `GET /api/v1/market/sectors` — gộp theo `icb_code`, dùng chung cho cả treemap (toàn bộ) lẫn dòng
  tiền (FE tự sort theo `|moneyFlow|`, lấy top 9) — 1 nguồn dữ liệu, 2 cách hiển thị, giống hệt cách
  mock cũ đã làm.
- **Không có market cap thật** (vnstock không trả shares outstanding) — `marketCapWeight` thực chất
  là **tỷ trọng turnover** (giá trị khớp lệnh), `moneyFlow` là heuristic
  (`turnover × dấu(percentChange)`), không phải order-flow thật. Ghi rõ trong javadoc
  `SectorSnapshotResponse` để không ai tưởng là dữ liệu chính xác.
- Bug phát hiện khi code: `price_board` trả `NaN` cho các mã không khớp lệnh, và FastAPI's JSON
  encoder crash thẳng trên `NaN` (watchlist nhỏ chưa bao giờ trúng mã nào NaN nên bug này ẩn từ đầu
  dự án) — `DataFrame.where(pd.notnull(df), None)` tưởng sửa được nhưng **không**, vì cột float64
  không giữ được `None` (numpy tự ép ngược về NaN); phải `.astype(object)` trước. Fix áp dụng chung
  cho mọi route ở `python/src/vst_python/api.py` (`_to_records`), không chỉ riêng `/price-board`.
- Trigger tay cho dev: `POST /api/v1/market/industries/sync`, `POST /api/v1/market/snapshot/ingest`
  (chỉ profile `dev`, mirror `IndexIngestionController`).
- Migration `V7__market_snapshot.sql`: `stocks.icb_code`/`icb_name`, bảng `daily_market_snapshot`.

## API đã có (tính đến hiện tại)

**Backend (`localhost:8080`)**

| Method | Endpoint | Phase |
|---|---|---|
| GET | `/api/v1/health` | 0 |
| GET | `/api/v1/stocks/{symbol}/ohlcv?start=&end=` | 2 |
| GET | `/api/v1/stocks?q=` | 2 |
| GET / POST | `/api/v1/watchlist` | 2 |
| DELETE | `/api/v1/watchlist/{symbol}` | 2 |
| STOMP | `/ws` (handshake), topic `/topic/price-board` | 3 |
| GET | `/api/v1/indices` | 4 |
| GET | `/api/v1/indices/{symbol}/ohlcv?start=&end=` | 4 |
| POST | `/api/v1/indices/ingest` (**chỉ profile `dev`**) | 4 |
| GET | `/api/v1/market/breadth` | 4b |
| GET | `/api/v1/market/sectors` | 4b |
| POST | `/api/v1/market/industries/sync` (**chỉ profile `dev`**) | 4b |
| POST | `/api/v1/market/snapshot/ingest` (**chỉ profile `dev`**) | 4b |

**Python service (`localhost:8000`, nội bộ — backend gọi, không phải public API)**

| Method | Endpoint | Phase | vnstock API đứng sau |
|---|---|---|---|
| GET | `/ohlcv?symbol=&start=&end=` | 1 | `Market().equity(symbol).ohlcv()` |
| GET | `/listing` | 2 | `Listing().symbols_by_exchange()` |
| GET | `/price-board?symbols=` | 3 | `Trading().price_board(symbols)` |
| GET | `/index-ohlcv?symbol=&start=&end=` | 4 | `Market().index(symbol).ohlcv()` |
| GET | `/industries` | 4b | `Listing(source="vci").symbols_by_industries()` |

vnstock còn nhiều class/API chưa dùng tới (`Company`, `Finance`, `Fundamental`, `Quote.intraday`,
`Fund`...) — tiềm năng cho phần "advisory" (phân tích cơ bản) ở các phase sau.

## Kế hoạch từ đây

- **Phase 4 + 4b (hoàn thành)**: trang chủ (kể cả breadth/treemap/dòng tiền) + trang chi tiết mã +
  chart OHLCV đều xong, không còn mock nào trên trang chủ.
- **Việc đang chờ cụ thể** (có ghi chi tiết trong `CLAUDE.md`): multi-user/auth cho watchlist.
  `price_snapshots` hypertable tạo từ Phase 1 vẫn chưa dùng.
- **Phase 5 (dự kiến, chưa chi tiết)**: nâng cao chất lượng test — Testcontainers, SonarQube,
  coverage threshold.
- **Phase 6-7**: chưa có tài liệu chốt chi tiết — xem `CLAUDE.md` cho các việc đang chờ, cần xác
  nhận lại với chủ dự án khi tới lúc.

## Cách chạy full stack (dev)

```bash
# 1. DB
docker compose up -d db

# 2. Python service
cd python && ./.venv/Scripts/python.exe -m uvicorn vst_python.api:app --host 0.0.0.0 --port 8000

# 3. Backend
cd be && ./mvnw.cmd spring-boot:run

# 4. Frontend
cd fe && npm run dev
```

Biến môi trường: xem `.env.example` ở root (`DB_*`, `VNSTOCK_SERVICE_URL`, các `*_CRON` để chỉnh
lịch chạy job nền).

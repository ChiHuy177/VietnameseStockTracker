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

**Còn thiếu**: `price_snapshots` hypertable (tạo từ Phase 1) vẫn chưa được dùng để lưu lại lịch sử
tick real-time — hiện tại data chỉ broadcast, không persist.

## Phase 4 — Frontend (đang làm)

**Tooling scaffold**: ESLint (thay `oxlint`, flat config), TanStack Query (`QueryClientProvider`),
Redux Toolkit (`store.ts`), React Router (`BrowserRouter`), `@stomp/stompjs`, Tailwind v4 + shadcn/ui
(viết tay theo chuẩn `new-york` — CLI `npx shadcn` bị treo/crash trong môi trường monorepo này),
`lightweight-charts` (cài, chưa dùng), Vitest + React Testing Library.

**Trang `/` (HomePage)** — duy nhất tính đến hiện tại:
- `SearchBox` — debounce 300ms, gọi `GET /api/v1/stocks?q=` qua TanStack Query, nút "+ Watchlist"
  trên mỗi kết quả (mutation, invalidate query watchlist khi thành công)
- `WatchlistTable` — `GET /api/v1/watchlist` qua TanStack Query, nút "Xóa" (mutation), cột **Giá**
  đọc real-time từ Redux (`priceBoardSlice`) — `useStompPriceBoard()` (chạy 1 lần ở `App.tsx`)
  subscribe `/topic/price-board`, dispatch quote mới nhất theo symbol vào store
- `HealthCheck` (Phase 0) không còn render trong `App.tsx` nữa (route `/` đã có nội dung thật để
  chứng minh FE↔BE), nhưng file feature vẫn giữ lại

**Còn thiếu**: trang chi tiết mã + chart OHLCV (`lightweight-charts` đã cài, chưa dùng), route nào
khác ngoài `/`.

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

**Python service (`localhost:8000`, nội bộ — backend gọi, không phải public API)**

| Method | Endpoint | Phase | vnstock API đứng sau |
|---|---|---|---|
| GET | `/ohlcv?symbol=&start=&end=` | 1 | `Market().equity(symbol).ohlcv()` |
| GET | `/listing` | 2 | `Listing().symbols_by_exchange()` |
| GET | `/price-board?symbols=` | 3 | `Trading().price_board(symbols)` |

vnstock còn nhiều class/API chưa dùng tới (`Company`, `Finance`, `Fundamental`, `Quote.intraday`,
`Fund`...) — tiềm năng cho phần "advisory" (phân tích cơ bản) ở các phase sau.

## Kế hoạch từ đây

- **Phase 4 (đang làm)**: trang chi tiết mã + chart OHLCV (`lightweight-charts`), thêm route ngoài
  `/`.
- **Phase 5 (dự kiến, chưa chi tiết)**: nâng cao chất lượng test — Testcontainers, SonarQube,
  coverage threshold.
- **Phase 6-7**: chưa có tài liệu chốt chi tiết trong repo (không có `CLAUDE.md`) — cần xác nhận
  lại với chủ dự án khi tới lúc.

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

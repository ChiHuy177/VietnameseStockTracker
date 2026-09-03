# Vietnamese Stock Tracker & Advisory Platform

Học tập kiến trúc enterprise-style: theo dõi giá chứng khoán Việt Nam gần real-time, sau đó mở
rộng thành hệ thống phân tích/gợi ý dựa trên dữ liệu lịch sử.

## Cấu trúc monorepo

```
be/       Backend API (Spring Boot, Java 21, Maven)
fe/       Frontend (React + TypeScript + Vite)
python/   Data fetching / ML microservice (vnstock)
```

Mỗi service là một thư mục độc lập trong cùng một repo, xây dựng và triển khai riêng biệt nhưng
chia sẻ lịch sử Git và quy trình review chung.

## Chạy backend (be/)

Không cần cài Maven — dùng Maven Wrapper đi kèm repo:

```bash
cd be
./mvnw spring-boot:run       # Windows: mvnw.cmd spring-boot:run
```

Mặc định chạy với profile `dev` (`SPRING_PROFILES_ACTIVE=dev`). Kiểm tra:

```bash
curl http://localhost:8080/api/v1/health
```

Chạy test:

```bash
./mvnw test
```

## Database (TimescaleDB qua Docker)

```bash
cp .env.example .env      # chỉnh giá trị nếu cần
docker compose up -d db
```

Backend đọc cấu hình kết nối DB qua biến môi trường (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`,
`DB_PASSWORD` — xem `.env.example`), migration Flyway trong
`be/src/main/resources/db/migration/` tự chạy khi backend khởi động.

Xem dữ liệu bằng pgAdmin (hoặc client bất kỳ): kết nối `localhost:5432`, database/user/password lấy
từ `.env`.

## Kiến trúc backend

3-tier cổ điển theo package:

- `controller/` — REST controller (ví dụ: `HealthController`)
- `service/` — business logic + orchestration (ví dụ: `HealthService`)
- `repository/` — data access: Spring Data repository, external API client (trống ở Phase 0, sẽ có từ Phase 1)
- `model/` — domain/entity object (ví dụ: `HealthStatus`)
- `dto/` — request/response shape cho API, tách biệt khỏi `model/` (ví dụ: `HealthResponse`)
- `exception/` — exception hierarchy + `GlobalExceptionHandler`

Lỗi trả về theo chuẩn RFC 7807 (`ProblemDetail`), xử lý tập trung tại `GlobalExceptionHandler`,
không try/catch rải rác trong controller/service. Mọi response lỗi có thêm 2 field ngoài chuẩn
RFC 7807 (`type`/`title`/`status`/`detail`/`instance`): `errorCode` (mã ổn định, máy đọc được, để
client branch theo code thay vì parse message) và `timestamp`.

`AppException` (abstract, base) là cha của:

| Exception | HTTP status | errorCode |
|---|---|---|
| `ValidationException` | 400 | `VALIDATION_ERROR` |
| `ResourceNotFoundException` | 404 | `RESOURCE_NOT_FOUND` |
| `ConflictException` | 409 | `CONFLICT` |
| `DataFetchException` | 502 | `DATA_FETCH_ERROR` |

Chưa gắn vào domain thật (Health không có trường hợp tự nhiên để ném notFound/conflict) — đây là
hierarchy dùng chung, các feature ở Phase 1+ (ví dụ `StockService`) sẽ ném các exception này thay
vì tự xử lý lỗi riêng lẻ.

## Chạy frontend (fe/)

```bash
cd fe
npm install
npm run dev
```

Mở `http://localhost:5173`. Dev server proxy mọi request `/api/*` sang backend ở `localhost:8080`
(cấu hình trong `vite.config.ts`) nên không cần bật CORS ở backend lúc dev.

Build production (chạy `tsc -b` với TypeScript strict mode trước, rồi `vite build`):

```bash
npm run build
```

## Kiến trúc frontend

```
src/
├── app/               # app shell (App.tsx)
├── models/            # type ứng với response/entity từ backend (ví dụ: health.ts)
├── services/           # gọi API bằng axios (httpClient.ts dùng chung + 1 service/domain)
│   ├── httpClient.ts
│   └── healthService.ts
└── features/
    └── health/         # component UI, dùng model + service ở trên
        └── HealthCheck.tsx
```

`models/` và `services/` là 2 tầng dùng chung (song song với `model/`/`dto/` bên backend):
`models/` định nghĩa shape dữ liệu, `services/` là nơi duy nhất gọi HTTP (qua `httpClient` — 1
axios instance cấu hình sẵn `baseURL: /api/v1`). Component trong `features/` chỉ gọi service, không
tự gọi `axios`/`fetch` trực tiếp. `features/` vẫn tổ chức theo nghiệp vụ — feature sau này (giá cổ
phiếu...) thêm 1 model + 1 service + 1 thư mục con trong `features/`.

## Python service (python/)

```bash
cd python
python -m venv .venv
source .venv/Scripts/activate      # Windows Git Bash; PowerShell: .venv\Scripts\Activate.ps1
pip install -e .
```

**Scaffold only (Phase 0)** — package `vst_python` chưa có logic fetch dữ liệu. Cấu trúc dùng
"src layout" (`src/vst_python/`) để tránh nhầm giữa code nguồn và bản đã cài. Phase 1 sẽ thêm
`vnstock` vào `dependencies` trong `pyproject.toml` và viết fetcher thật.

## CI (GitHub Actions)

`.github/workflows/ci.yml` chạy 3 job song song trên mọi push/PR vào `main`: `backend` (`mvnw verify`),
`frontend` (`npm ci && npm run lint && npm run build`), `python` (`pip install -e .` rồi import thử).
Chưa có bước deploy — CI hiện tại chỉ để bắt lỗi build/test sớm, đúng tinh thần tối giản của Phase 0.

## Tiến độ

- [x] Phase 0 — Bước 1: Root + Backend skeleton (health endpoint, exception handling, config profiles)
- [x] Phase 0 — Bước 2: Docker Compose + TimescaleDB
- [x] Phase 0 — Bước 3: Frontend scaffold
- [x] Phase 0 — Bước 4: Python service scaffold
- [x] Phase 0 — Bước 5: CI (GitHub Actions)

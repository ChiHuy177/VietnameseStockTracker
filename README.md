# Vietnamese Stock Tracker & Advisory Platform

Học tập kiến trúc enterprise-style: theo dõi giá chứng khoán Việt Nam gần real-time, sau đó mở
rộng thành hệ thống phân tích/gợi ý dựa trên dữ liệu lịch sử.

## Cấu trúc monorepo

```
be/       Backend API (Spring Boot, Java 21, Maven)
fe/       Frontend (React + TypeScript + Vite)
python/   Data fetching / ML microservice (vnstock)      — thêm ở bước sau
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
- `exception/` — exception hierarchy (`AppException` và các lớp con) + `GlobalExceptionHandler`

Lỗi trả về theo chuẩn RFC 7807 (`ProblemDetail`), xử lý tập trung tại `GlobalExceptionHandler`,
không try/catch rải rác trong controller/service.

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
└── features/
    └── health/        # gọi GET /api/v1/health, hiển thị trạng thái backend
        ├── api.ts
        ├── types.ts
        └── HealthCheck.tsx
```

Tổ chức theo feature (không theo loại file như `components/`, `hooks/`) — mỗi feature nghiệp vụ
sau này (giá cổ phiếu, danh mục theo dõi...) sẽ là 1 thư mục con trong `features/`, gói gọn API
call, type, và component liên quan.

## Tiến độ

- [x] Phase 0 — Bước 1: Root + Backend skeleton (health endpoint, exception handling, config profiles)
- [x] Phase 0 — Bước 2: Docker Compose + TimescaleDB
- [x] Phase 0 — Bước 3: Frontend scaffold
- [ ] Phase 0 — Bước 4: Python service scaffold
- [ ] Phase 0 — Bước 5: CI (GitHub Actions)

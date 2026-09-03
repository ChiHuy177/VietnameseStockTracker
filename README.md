# Vietnamese Stock Tracker & Advisory Platform

Học tập kiến trúc enterprise-style: theo dõi giá chứng khoán Việt Nam gần real-time, sau đó mở
rộng thành hệ thống phân tích/gợi ý dựa trên dữ liệu lịch sử.

## Cấu trúc monorepo

```
be/       Backend API (Spring Boot, Java 21, Maven)
fe/       Frontend (React + TypeScript + Vite)          — thêm ở bước sau
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

## Tiến độ

- [x] Phase 0 — Bước 1: Root + Backend skeleton (health endpoint, exception handling, config profiles)
- [ ] Phase 0 — Bước 2: Docker Compose + TimescaleDB
- [ ] Phase 0 — Bước 3: Frontend scaffold
- [ ] Phase 0 — Bước 4: Python service scaffold
- [ ] Phase 0 — Bước 5: CI (GitHub Actions)

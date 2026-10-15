# CLAUDE.md

Hướng dẫn cho Claude Code khi làm việc trong repo này.

## Tổng quan

Xem [docs/PROGRESS.md](docs/PROGRESS.md) để biết đầy đủ kiến trúc, luồng dữ liệu và tiến độ từng
phase (0-4). File này chỉ dùng để ghi lại các việc **đang chờ làm** mà một phiên trước đã cố ý để
lại — tránh bị quên giữa các phiên làm việc.

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

## Trang chủ kiểu Fireant — ĐÃ XONG, KHÔNG CÒN VIỆC

Cả 3 phần (chỉ số thị trường, độ rộng thị trường, treemap/dòng tiền theo ngành) đã có BE lẫn FE
thật, không còn dùng mock. Kiến trúc + luồng dữ liệu đầy đủ xem `docs/PROGRESS.md` (Phase 4). Giữ
lại tiêu đề này một thời gian để khỏi ai tưởng còn việc treo — xoá hẳn mục này ở phiên dọn dẹp sau.

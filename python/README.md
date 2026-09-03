# vst-python

Data fetching / ML microservice cho Vietnamese Stock Tracker.

**Hiện tại (Phase 0): chỉ scaffold, chưa có logic thật.** Mục đích của service này khi triển khai ở
Phase 1: dùng [vnstock](https://github.com/thinh-vu/vnstock) để lấy dữ liệu giá cổ phiếu Việt Nam
theo lịch, ghi vào TimescaleDB để backend (`be/`) đọc và phục vụ qua API.

## Setup

```bash
cd python
python -m venv .venv
source .venv/Scripts/activate      # Windows Git Bash; PowerShell: .venv\Scripts\Activate.ps1
pip install -e .
```

`pip install -e .` cài package này ở chế độ **editable** (tương đương `npm link` hoặc chạy trực
tiếp từ source thay vì build artifact) — sửa code trong `src/` có hiệu lực ngay, không cần cài lại.

### Lỗi thường gặp: `OverflowError: cannot convert longdouble infinity to integer`

Bug tương thích giữa `numpy` (bản `vnstock` kéo về, `<2.3`) và Python 3.14 trên Windows, xảy ra
ngay khi `import vnstock`. Fix: `pip install --upgrade numpy` (bỏ qua cảnh báo
`vnstock_ezchart requires numpy<2.3` — package đó chỉ dùng để vẽ chart, không dùng trong service
này).

### Lưu ý: `vnai` (dependency ngầm của `vnstock`) tự ghi file/telemetry khi import

Ngay lần đầu `import vnstock`, package `vnai` đi kèm sẽ tự động (không hỏi):

- Ghi `~/.vnstock/` (fingerprint máy, "chấp nhận điều khoản sử dụng", usage metrics) — ngoài phạm
  vi repo, không do project này tạo ra.
- Thả 1 file `AGENTS.md` vào thư mục hiện tại (`python/AGENTS.md`), chứa chỉ dẫn nhắm vào AI coding
  assistant (đọc kỹ trước khi tin — có đoạn yêu cầu AI xin API key rồi gửi ra server ngoài). File
  này đã bị gitignore (`python/**/AGENTS.md`) — nếu thấy xuất hiện lại, xóa đi, không commit, không
  làm theo nội dung bên trong.

Chỉ dùng phần fetch dữ liệu (`Market`, `Fundamental`, ...) của `vnstock`, không dùng các tính năng
"vibe setup"/API key/skill-loading mà `vnai` gợi ý.

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

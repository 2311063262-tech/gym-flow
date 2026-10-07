# GymFlow Membership Frontend

React + TypeScript + Vite frontend cho `membership-service`.

## Cấu hình
- **Port:** 3000
- **Framework:** React 18 + TypeScript + Vite
- **Styling:** Tailwind CSS

## Yêu cầu
- Node.js 18+
- npm hoặc yarn

## Cấu hình Environment
File `.env`:
```
VITE_API_BASE_URL=http://localhost:8080
```

## Cách chạy
```bash
npm install
npm run dev
```

Trước khi dùng giao diện, khởi động MySQL, `membership-service` ở cổng 8082 và
`api-gateway` ở cổng 8080. Giao diện mặc định truy cập qua
`http://localhost:8080` theo cấu hình trong `.env`.

## Build cho production
```bash
npm run build
```

## Tính năng
- React 18 với TypeScript
- Vite cho development nhanh
- Tailwind CSS cho styling
- React Router DOM cho điều hướng giữa các trang membership
- Axios cho API calls
- Quản lý gói tập, membership và thanh toán

## Trang
- `/plans` - Xem, tạo, sửa và xóa gói tập
- `/memberships` - Đăng ký membership và cập nhật trạng thái
- `/payments` - Xem và tạo thanh toán

Đăng ký membership cần `memberId` do service quản lý hội viên cung cấp. Khi tạo
membership, backend tự tạo một khoản thanh toán ở trạng thái `PENDING`.

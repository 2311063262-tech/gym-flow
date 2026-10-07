# Membership Service - GymFlow Microservices

## Mô tả
Service quản lý gói tập, thành viên và thanh toán trong hệ thống GymFlow.

## Cấu hình
- **Port:** 8082
- **Database:** membership_db (MySQL)
- **Framework:** Spring Boot 3.2.0

## Yêu cầu
- Java 17+
- Maven 3.6+
- MySQL 8.0+

## Cấu hình Database
Tạo database MySQL:
```sql
CREATE DATABASE membership_db;
```

Thiết lập các biến môi trường trước khi chạy service:
```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<mat-khau-mysql>"
$env:JWT_SECRET = "<chuoi-bi-mat-ngau-nhien-it-nhat-32-ky-tu>"
```

## Cách chạy
```bash
mvn clean install
mvn spring-boot:run
```

`DB_USERNAME` mặc định là `root`; `DB_PASSWORD` và `JWT_SECRET` là bắt buộc.
Không lưu các giá trị thật trong source code hoặc commit lên Git.

## API Endpoints

### Public Endpoints
- `GET /api/plans` - Lấy danh sách gói tập

### Internal Endpoints (chỉ service-to-service)
- `GET /internal/memberships/{id}/validate` - Validate membership

### Protected Endpoints (cần JWT)
- `POST /api/plans` - Tạo gói tập mới (ADMIN/STAFF)
- `GET /api/memberships` - Lấy danh sách thành viên
- `POST /api/memberships` - Đăng ký thành viên mới
- `GET /api/memberships/{id}` - Lấy chi tiết thành viên
- `POST /api/payments` - Tạo thanh toán mới

## Tính năng
- Quản lý gói tập (Plans)
- Quản lý thành viên (Memberships)
- Quản lý thanh toán (Payments)
- Internal APIs cho inter-service communication
- Role-based access control

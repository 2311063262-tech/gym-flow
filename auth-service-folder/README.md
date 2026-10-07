# Auth Service - GymFlow Microservices

## Mô tả
Service quản lý xác thực và người dùng trong hệ thống GymFlow.

## Cấu hình
- **Port:** 8081
- **Database:** auth_db (MySQL)
- **Framework:** Spring Boot 3.2.0

## Yêu cầu
- Java 21
- Maven 3.6+
- MySQL 8.0+

## Cấu hình Database
Tạo/cập nhật schema theo file SQL:
```sql
SOURCE src/main/resources/db/auth_db.sql;
```
Hoặc chạy từ thư mục gốc của service:
```bash
mysql -u root -p < src/main/resources/db/auth_db.sql
```
Script không xóa/recreate bảng hoặc tài khoản hiện có; nó bổ sung các cột cần thiết và tạo bảng `revoked_tokens`. Với schema legacy có `role_id` và bảng `roles`, script chuyển role trước khi bỏ cột legacy `role_id`. Sao lưu database trước khi chạy migration trên môi trường có dữ liệu quan trọng.

Script dùng `INSERT IGNORE` để thêm tài khoản mẫu khi username/email chưa tồn tại. Các tài khoản đã có sẽ không bị thay đổi password hash hay thông tin:

| Role | Email | Mật khẩu |
|---|---|---|
| ADMIN | `admin@gymflow.com` | `GymFlowAdmin123!` |
| TRAINER | `trainer1@gymflow.com` | `GymFlowTrainer123!` |
| MEMBER | `member1@gymflow.com` | `GymFlowMember123!` |

Mật khẩu trong database được lưu bằng BCrypt. Chỉ dùng tài khoản mẫu trong môi trường phát triển/thử nghiệm, không dùng trên production.

Cấu hình datasource trong `src/main/resources/application.yml` dùng `DB_USERNAME` (mặc định `root`) và `DB_PASSWORD`. Khi không đặt `JWT_SECRET`, service tự tạo khóa ngẫu nhiên tạm thời để hỗ trợ chạy local; log sẽ cảnh báo rằng token hiện tại mất hiệu lực khi service khởi động lại. Để giữ phiên đăng nhập qua các lần khởi động, hãy cấu hình `JWT_SECRET` ổn định, dài tối thiểu 32 byte. Không commit thông tin nhạy cảm vào source code.

Ví dụ chạy trên PowerShell:
```powershell
$env:DB_USERNAME = "root"
$securePassword = Read-Host "Nhập mật khẩu MySQL" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
$secretBytes = New-Object byte[] 48
$random = [Security.Cryptography.RandomNumberGenerator]::Create()
$random.GetBytes($secretBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
$random.Dispose()
try {
    mvn spring-boot:run
} finally {
    Remove-Item Env:DB_USERNAME, Env:DB_PASSWORD, Env:JWT_SECRET -ErrorAction SilentlyContinue
    Remove-Variable securePassword, secretBytes -ErrorAction SilentlyContinue
}
```
Nếu dùng tài khoản MySQL khác, cập nhật `DB_USERNAME`. Trong IntelliJ, khai báo ba biến `DB_USERNAME`, `DB_PASSWORD` và `JWT_SECRET` trong Environment variables của cấu hình Run/Debug.

## Cách chạy
```bash
mvn clean test
mvn spring-boot:run
```

Sau khi service khởi động, mở `http://localhost:8081/` để sử dụng giao diện đăng nhập và đăng ký. Giao diện được phục vụ trực tiếp từ `src/main/resources/static` và gọi API trên cùng origin.

Đăng nhập thành công sẽ điều hướng ADMIN đến `/admin/dashboard`, STAFF đến `/staff`, TRAINER đến `/trainer` và MEMBER đến `/member`. Các trang quản trị gồm `/admin/dashboard`, `/admin/users` và `/admin/profile`; API vẫn tự xác minh JWT và role ở backend.

## API Endpoints
- `POST /api/auth/register` - Đăng ký công khai; role luôn là MEMBER
- `POST /api/auth/login` - Đăng nhập; từ chối tài khoản bị vô hiệu hóa/xóa mềm
- `GET /api/auth/me` - Lấy user hiện tại từ danh tính JWT
- `POST /api/auth/logout` - Thu hồi JTI của token hiện tại
- `GET /api/auth/users` - Tìm kiếm/lọc/phân trang tài khoản (ADMIN/STAFF; STAFF chỉ đọc)
- `GET /api/auth/users/stats` - Số liệu dashboard (ADMIN)
- `GET /api/auth/users/{id}` - Chi tiết user (ADMIN/STAFF hoặc chính chủ)
- `POST /api/auth/users` - Tạo TRAINER/MEMBER (ADMIN)
- `PUT /api/auth/users/{id}` - Cập nhật hồ sơ chính chủ; ADMIN có thể sửa TRAINER/MEMBER
- `PATCH /api/auth/users/{id}/status` - Kích hoạt/vô hiệu hóa TRAINER/MEMBER (ADMIN)
- `DELETE /api/auth/users/{id}` - Xóa mềm TRAINER/MEMBER (ADMIN)

## Tính năng
- JWT Authentication
- Role-based Access Control (ADMIN, STAFF, TRAINER, MEMBER)
- Password encryption với BCrypt
- Global exception handling
- Giao diện dashboard quản trị responsive, số liệu và danh sách lấy từ API
- Response user không bao gồm password hoặc password hash

## Frontend development
Frontend là HTML/CSS/JavaScript tĩnh được Spring Boot phục vụ cùng origin; không cần chạy một dev server frontend riêng. Tài khoản mẫu chỉ được thêm bởi script SQL nếu chưa tồn tại, nên mật khẩu mẫu không áp dụng cho tài khoản trùng username/email đã có.

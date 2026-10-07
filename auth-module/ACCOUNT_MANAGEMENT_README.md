# Chức Năng Quản Lý Tài Khoản - Admin

## Tổng quan

Hệ thống quản lý tài khoản cho phép ADMIN quản lý toàn bộ tài khoản người dùng trong hệ thống Gym Management, bao gồm TRAINER và MEMBER.

## Các chức năng đã triển khai

### 1. Xem danh sách tài khoản ✅
- Hiển thị: username, email, role, trạng thái (is_active), ngày tạo, lần đăng nhập gần nhất
- Chỉ hiển thị tài khoản chưa bị xóa mềm (deleted_at = NULL)

### 2. Tìm kiếm và lọc ✅
- **Tìm kiếm:** theo username hoặc email (case-insensitive, partial match)
- **Lọc theo role:** TRAINER hoặc MEMBER
- **Lọc theo trạng thái:** is_active (true/false)
- Có thể kết hợp cả 3 tiêu chí

### 3. Xem chi tiết tài khoản ✅
- Hiển thị đầy đủ thông tin cơ bản của tài khoản
- Không hiển thị password

### 4. Tạo tài khoản mới ✅
- Tạo tài khoản TRAINER hoặc MEMBER
- Gán role và đặt mật khẩu ban đầu
- Validate: username unique, email unique (nếu có)
- Password được hash bằng BCrypt
- Không cho phép tạo tài khoản ADMIN

### 5. Cập nhật tài khoản ✅
- Sửa username, email, role, fullName, phone
- Validate: không trùng username/email với tài khoản khác
- Không thể thay đổi role thành ADMIN
- Admin không thể tự cập nhật tài khoản của mình

### 6. Kích hoạt/Vô hiệu hóa tài khoản ✅
- Sử dụng trường `is_active` (BOOLEAN)
- Tài khoản bị vô hiệu hóa không thể đăng nhập
- Admin không thể tự vô hiệu hóa tài khoản của mình
- Không cho phép vô hiệu hóa tài khoản ADMIN khác

### 7. Xóa mềm tài khoản ✅
- Set `deleted_at = current_timestamp`
- Đồng thời set `is_active = false`
- Tài khoản đã xóa mềm:
  - Không xuất hiện trong danh sách
  - Không thể đăng nhập
  - Không bị xóa hẳn khỏi database
- Admin không thể tự xóa tài khoản của mình
- Không cho phép xóa tài khoản ADMIN khác

## Cấu trúc code mới

### Entities
- ✅ **User.java** - Đã cập nhật thêm:
  - `isActive` (Boolean)
  - `createdAt` (LocalDateTime)
  - `lastLoginAt` (LocalDateTime)
  - `deletedAt` (LocalDateTime)

### DTOs
- ✅ **AccountResponse.java** - Response cho admin với đầy đủ thông tin
- ✅ **CreateAccountRequest.java** - Request tạo tài khoản mới với validation
- ✅ **UpdateAccountRequest.java** - Request cập nhật tài khoản với validation

### Repositories
- ✅ **UserRepository.java** - Đã thêm query methods:
  - `findAllActive()` - Lấy tất cả tài khoản chưa xóa
  - `findByIdAndNotDeleted()` - Tìm theo ID và chưa xóa
  - `findByUsernameAndNotDeleted()` - Tìm theo username và chưa xóa
  - `searchByUsernameOrEmail()` - Tìm kiếm theo username hoặc email
  - `findByRole()` - Lọc theo role
  - `findByIsActive()` - Lọc theo trạng thái
  - `findByFilters()` - Tìm kiếm và lọc kết hợp
  - `existsByEmail()` - Kiểm tra email tồn tại

### Services
- ✅ **AdminAccountService.java** - Service mới cho quản lý tài khoản:
  - `getAllAccounts()` - Lấy danh sách tất cả
  - `searchAndFilterAccounts()` - Tìm kiếm và lọc
  - `getAccountById()` - Xem chi tiết
  - `createAccount()` - Tạo tài khoản mới
  - `updateAccount()` - Cập nhật thông tin
  - `activateAccount()` - Kích hoạt
  - `deactivateAccount()` - Vô hiệu hóa
  - `softDeleteAccount()` - Xóa mềm
  - `getCurrentAdmin()` - Kiểm tra quyền ADMIN
  - `toAccountResponse()` - Convert entity sang DTO

- ✅ **AuthService.java** - Đã cập nhật:
  - Kiểm tra tài khoản đã xóa mềm (deleted_at != NULL)
  - Kiểm tra tài khoản có active không (is_active = true)
  - Cập nhật `lastLoginAt` khi đăng nhập thành công

### Controllers
- ✅ **AdminAccountController.java** - REST Controller mới:
  - `GET /api/admin/accounts` - Danh sách tất cả
  - `GET /api/admin/accounts/search` - Tìm kiếm và lọc
  - `GET /api/admin/accounts/{id}` - Chi tiết
  - `POST /api/admin/accounts` - Tạo mới
  - `PUT /api/admin/accounts/{id}` - Cập nhật
  - `PUT /api/admin/accounts/{id}/activate` - Kích hoạt
  - `PUT /api/admin/accounts/{id}/deactivate` - Vô hiệu hóa
  - `DELETE /api/admin/accounts/{id}` - Xóa mềm

### Database
- ✅ **update_users_table_for_account_management.sql** - Script cập nhật database:
  - Thêm cột `is_active` (BOOLEAN, default TRUE)
  - Thêm cột `created_at` (DATETIME, auto timestamp)
  - Thêm cột `last_login_at` (DATETIME, nullable)
  - Thêm cột `deleted_at` (DATETIME, nullable)
  - Tạo indexes cho tối ưu query

### Dependencies
- ✅ **pom.xml** - Đã thêm:
  - `spring-boot-starter-validation` - Validation cho DTO

## Quy tắc nghiệp vụ được tuân thủ

### ✅ Authorization
- Chỉ tài khoản có role ADMIN mới được phép gọi API
- Kiểm tra quyền trong mỗi method của AdminAccountService

### ✅ Bảo mật Password
- Không trả trường password về frontend (dùng AccountResponse)
- Password được hash bằng BCrypt khi tạo hoặc đổi mật khẩu
- BCrypt strength = 10 (mặc định)

### ✅ Quản lý Role
- Không cho tạo role tùy ý
- Chỉ chấp nhận role TRAINER và MEMBER khi tạo/cập nhật
- Không cho phép tạo hoặc thay đổi thành role ADMIN

### ✅ Bảo vệ tài khoản Admin
- Admin không thể tự vô hiệu hóa tài khoản của mình
- Admin không thể tự xóa tài khoản của mình
- Admin không thể tự cập nhật tài khoản của mình qua API này
- Không cho phép vô hiệu hóa hoặc xóa tài khoản ADMIN khác

### ✅ Soft Delete
- Tài khoản đã xóa mềm (deleted_at != NULL):
  - Không xuất hiện trong danh sách mặc định
  - Không được đăng nhập
  - Không bị xóa hẳn khỏi database
- Đồng thời set is_active = false khi xóa mềm

### ✅ Validation
- Username: 3-100 ký tự, chỉ chữ cái, số và dấu gạch dưới
- Password: 6-50 ký tự (khi tạo mới)
- Email: Format email hợp lệ
- Phone: 10-15 chữ số
- Role: Chỉ TRAINER hoặc MEMBER
- Username và email phải unique

## Hướng dẫn cài đặt và sử dụng

### 1. Cập nhật Database
```bash
# Chạy script SQL để thêm các cột mới
mysql -u root -p auth_db < sql/update_users_table_for_account_management.sql
```

### 2. Build và chạy ứng dụng
```bash
cd auth-module/backend
mvn clean install
mvn spring-boot:run
```

### 3. Test API
```bash
# 1. Login để lấy JWT token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Sử dụng token để gọi API quản lý tài khoản
curl -X GET http://localhost:8081/api/admin/accounts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Chi tiết API xem file: [API_ADMIN_ACCOUNT_MANAGEMENT.md](./API_ADMIN_ACCOUNT_MANAGEMENT.md)

## Testing

### Unit Tests (Cần thêm)
- [ ] AdminAccountServiceTest.java
- [ ] AdminAccountControllerTest.java

### Integration Tests (Cần thêm)
- [ ] API integration tests với MockMvc
- [ ] Database integration tests

## Frontend Integration (Chưa có)

Frontend cần implement:
- [ ] Trang danh sách tài khoản với search và filter
- [ ] Form tạo tài khoản mới
- [ ] Form cập nhật tài khoản
- [ ] Nút kích hoạt/vô hiệu hóa tài khoản
- [ ] Confirm dialog cho xóa mềm tài khoản
- [ ] Hiển thị trạng thái is_active và ngày tạo/đăng nhập

## Security Notes

### ✅ Đã triển khai
- BCrypt password hashing
- JWT authentication
- Role-based access control
- Soft delete thay vì hard delete
- Validation input data
- Admin không thể tự xóa/vô hiệu hóa mình
- Tài khoản inactive/deleted không thể đăng nhập

### 🔜 Cần cải thiện (nếu cần)
- Password strength policy (độ phức tạp)
- Rate limiting cho API
- Audit logging (ghi lại các thao tác của admin)
- Email verification khi tạo tài khoản
- Password reset functionality
- Account lockout sau nhiều lần đăng nhập sai
- Two-factor authentication (2FA)

## Performance Optimization

### ✅ Đã triển khai
- Indexes trên các cột thường dùng để query:
  - `idx_users_deleted_at` - Tối ưu query tài khoản chưa xóa
  - `idx_users_is_active` - Tối ưu lọc theo trạng thái
  - `idx_users_role` - Tối ưu lọc theo role
  - `idx_users_email` - Tối ưu kiểm tra email unique

### 🔜 Cần cải thiện (nếu cần)
- Pagination cho danh sách tài khoản
- Caching với Redis
- Bulk operations (activate/deactivate nhiều tài khoản cùng lúc)

## Changelog

### Version 1.0.0 (Current)
- ✅ Thêm chức năng quản lý tài khoản đầy đủ
- ✅ CRUD operations cho tài khoản
- ✅ Tìm kiếm và lọc
- ✅ Kích hoạt/vô hiệu hóa
- ✅ Soft delete
- ✅ Validation và security rules
- ✅ API documentation

## Support

Để báo lỗi hoặc đề xuất tính năng mới, vui lòng tạo issue trong repository.

---

**Tác giả:** Kiro AI Assistant  
**Ngày tạo:** 2024-02-21  
**Phiên bản:** 1.0.0

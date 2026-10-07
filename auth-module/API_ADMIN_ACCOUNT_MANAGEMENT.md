# API Quản Lý Tài Khoản cho ADMIN

## Tổng quan

API này cung cấp các chức năng quản lý tài khoản dành cho ADMIN trong hệ thống Gym Management. Tất cả các endpoints đều yêu cầu JWT token và role ADMIN.

**Base URL:** `http://localhost:8081/api/admin/accounts`

**Authentication:** Bearer Token (JWT)

**Authorization:** Chỉ role `ADMIN` được phép truy cập

---

## Endpoints

### 1. Lấy danh sách tất cả tài khoản

**GET** `/api/admin/accounts`

Hiển thị danh sách tất cả tài khoản chưa bị xóa mềm.

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "username": "trainer01",
    "email": "trainer01@gym.com",
    "role": "TRAINER",
    "fullName": "Nguyễn Văn A",
    "phone": "0901234567",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "lastLoginAt": "2024-02-20T14:25:00"
  },
  {
    "id": 2,
    "username": "member01",
    "email": "member01@gmail.com",
    "role": "MEMBER",
    "fullName": "Trần Thị B",
    "phone": "0909876543",
    "isActive": true,
    "createdAt": "2024-01-20T09:15:00",
    "lastLoginAt": "2024-02-21T08:00:00"
  }
]
```

**Response 403 Forbidden:**
```json
{
  "error": "Chỉ ADMIN mới có quyền thực hiện thao tác này"
}
```

---

### 2. Tìm kiếm và lọc tài khoản

**GET** `/api/admin/accounts/search`

Tìm kiếm theo username/email và lọc theo role, trạng thái.

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:**
- `search` (optional): Tìm theo username hoặc email
- `role` (optional): Lọc theo role (`TRAINER` hoặc `MEMBER`)
- `isActive` (optional): Lọc theo trạng thái (`true` hoặc `false`)

**Example:**
```
GET /api/admin/accounts/search?search=trainer&role=TRAINER&isActive=true
```

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "username": "trainer01",
    "email": "trainer01@gym.com",
    "role": "TRAINER",
    "fullName": "Nguyễn Văn A",
    "phone": "0901234567",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00",
    "lastLoginAt": "2024-02-20T14:25:00"
  }
]
```

**Response 400 Bad Request:**
```json
{
  "error": "Role không hợp lệ. Chỉ chấp nhận TRAINER hoặc MEMBER"
}
```

---

### 3. Xem chi tiết tài khoản

**GET** `/api/admin/accounts/{id}`

Xem thông tin chi tiết của một tài khoản.

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters:**
- `id` (Long): ID của tài khoản

**Example:**
```
GET /api/admin/accounts/1
```

**Response 200 OK:**
```json
{
  "id": 1,
  "username": "trainer01",
  "email": "trainer01@gym.com",
  "role": "TRAINER",
  "fullName": "Nguyễn Văn A",
  "phone": "0901234567",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "lastLoginAt": "2024-02-20T14:25:00"
}
```

**Response 404 Not Found:**
```json
{
  "error": "Không tìm thấy tài khoản với ID: 999"
}
```

---

### 4. Tạo tài khoản mới

**POST** `/api/admin/accounts`

Tạo tài khoản TRAINER hoặc MEMBER mới.

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "trainer02",
  "password": "password123",
  "role": "TRAINER",
  "email": "trainer02@gym.com",
  "fullName": "Lê Văn C",
  "phone": "0912345678"
}
```

**Validation Rules:**
- `username`: Required, 3-100 ký tự, chỉ chữ cái, số và dấu gạch dưới
- `password`: Required, 6-50 ký tự
- `role`: Required, chỉ `TRAINER` hoặc `MEMBER` (không được tạo ADMIN)
- `email`: Optional, phải đúng format email
- `phone`: Optional, 10-15 chữ số
- `fullName`: Optional

**Response 201 Created:**
```json
{
  "id": 5,
  "username": "trainer02",
  "email": "trainer02@gym.com",
  "role": "TRAINER",
  "fullName": "Lê Văn C",
  "phone": "0912345678",
  "isActive": true,
  "createdAt": "2024-02-21T10:00:00",
  "lastLoginAt": null
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Dữ liệu không hợp lệ",
  "fields": {
    "username": "Username phải từ 3 đến 100 ký tự",
    "password": "Password phải từ 6 đến 50 ký tự"
  }
}
```

```json
{
  "error": "Username đã tồn tại: trainer02"
}
```

```json
{
  "error": "Email đã tồn tại: trainer02@gym.com"
}
```

```json
{
  "error": "Không được phép tạo tài khoản ADMIN"
}
```

---

### 5. Cập nhật thông tin tài khoản

**PUT** `/api/admin/accounts/{id}`

Cập nhật thông tin tài khoản (không bao gồm password).

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Path Parameters:**
- `id` (Long): ID của tài khoản

**Request Body:**
```json
{
  "username": "trainer02_updated",
  "email": "trainer02_new@gym.com",
  "role": "MEMBER",
  "fullName": "Lê Văn C Updated",
  "phone": "0912345679"
}
```

**Notes:**
- Tất cả các trường đều optional
- Không thể cập nhật tài khoản của chính mình
- Không thể thay đổi role thành ADMIN
- Chỉ chấp nhận role `TRAINER` hoặc `MEMBER`

**Response 200 OK:**
```json
{
  "id": 5,
  "username": "trainer02_updated",
  "email": "trainer02_new@gym.com",
  "role": "MEMBER",
  "fullName": "Lê Văn C Updated",
  "phone": "0912345679",
  "isActive": true,
  "createdAt": "2024-02-21T10:00:00",
  "lastLoginAt": null
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Không thể cập nhật tài khoản của chính mình qua chức năng này"
}
```

```json
{
  "error": "Username đã tồn tại: trainer02_updated"
}
```

```json
{
  "error": "Không được phép thay đổi role thành ADMIN"
}
```

---

### 6. Kích hoạt tài khoản

**PUT** `/api/admin/accounts/{id}/activate`

Kích hoạt tài khoản (set `isActive = true`).

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters:**
- `id` (Long): ID của tài khoản

**Example:**
```
PUT /api/admin/accounts/5/activate
```

**Response 200 OK:**
```json
{
  "id": 5,
  "username": "trainer02",
  "email": "trainer02@gym.com",
  "role": "TRAINER",
  "fullName": "Lê Văn C",
  "phone": "0912345678",
  "isActive": true,
  "createdAt": "2024-02-21T10:00:00",
  "lastLoginAt": null
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Không thể kích hoạt/vô hiệu hóa tài khoản của chính mình"
}
```

---

### 7. Vô hiệu hóa tài khoản

**PUT** `/api/admin/accounts/{id}/deactivate`

Vô hiệu hóa tài khoản (set `isActive = false`). Tài khoản bị vô hiệu hóa không thể đăng nhập.

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters:**
- `id` (Long): ID của tài khoản

**Example:**
```
PUT /api/admin/accounts/5/deactivate
```

**Response 200 OK:**
```json
{
  "id": 5,
  "username": "trainer02",
  "email": "trainer02@gym.com",
  "role": "TRAINER",
  "fullName": "Lê Văn C",
  "phone": "0912345678",
  "isActive": false,
  "createdAt": "2024-02-21T10:00:00",
  "lastLoginAt": null
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Không thể kích hoạt/vô hiệu hóa tài khoản của chính mình"
}
```

```json
{
  "error": "Không được phép vô hiệu hóa tài khoản ADMIN"
}
```

---

### 8. Xóa mềm tài khoản

**DELETE** `/api/admin/accounts/{id}`

Xóa mềm tài khoản (set `deletedAt = current_timestamp` và `isActive = false`). Tài khoản đã xóa mềm không xuất hiện trong danh sách và không thể đăng nhập.

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters:**
- `id` (Long): ID của tài khoản

**Example:**
```
DELETE /api/admin/accounts/5
```

**Response 200 OK:**
```json
{
  "message": "Xóa tài khoản thành công"
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Không thể xóa tài khoản của chính mình"
}
```

```json
{
  "error": "Không được phép xóa tài khoản ADMIN"
}
```

---

## Quy tắc nghiệp vụ

### 1. Authorization
- Tất cả endpoints yêu cầu JWT token hợp lệ
- Chỉ tài khoản có role `ADMIN` mới được phép truy cập
- Admin không thể tự vô hiệu hóa/xóa tài khoản của mình

### 2. Tạo tài khoản
- Chỉ cho phép tạo role `TRAINER` hoặc `MEMBER`
- Không cho phép tạo tài khoản `ADMIN`
- Username và email phải unique
- Password được hash bằng BCrypt trước khi lưu vào database
- Tài khoản mới mặc định có `isActive = true`

### 3. Cập nhật tài khoản
- Admin không thể cập nhật tài khoản của chính mình qua API này
- Không thể thay đổi role thành `ADMIN`
- Chỉ chấp nhận role `TRAINER` hoặc `MEMBER`
- Username và email mới phải unique

### 4. Vô hiệu hóa tài khoản
- Admin không thể tự vô hiệu hóa tài khoản của mình
- Không cho phép vô hiệu hóa tài khoản `ADMIN` khác
- Tài khoản bị vô hiệu hóa không thể đăng nhập

### 5. Xóa mềm tài khoản
- Admin không thể tự xóa tài khoản của mình
- Không cho phép xóa tài khoản `ADMIN` khác
- Tài khoản đã xóa mềm:
  - Không xuất hiện trong danh sách
  - Không thể đăng nhập
  - Tự động set `isActive = false`
  - Không xóa hẳn khỏi database (soft delete)

### 6. Bảo mật
- Password không bao giờ được trả về trong response
- Password được hash bằng BCrypt (strength = 10)
- JWT token có thời gian expire 24 giờ
- Tài khoản bị xóa mềm không thể đăng nhập

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Thông báo lỗi cụ thể"
}
```

### 403 Forbidden
```json
{
  "error": "Chỉ ADMIN mới có quyền thực hiện thao tác này"
}
```

### 404 Not Found
```json
{
  "error": "Không tìm thấy tài khoản với ID: {id}"
}
```

### 500 Internal Server Error
```json
{
  "error": "Lỗi hệ thống: {error_message}"
}
```

---

## Cập nhật Database

Trước khi sử dụng API, cần chạy script SQL để cập nhật database:

```bash
mysql -u root -p < sql/update_users_table_for_account_management.sql
```

Script này sẽ thêm các cột:
- `is_active` (BOOLEAN): Trạng thái kích hoạt
- `created_at` (DATETIME): Ngày tạo tài khoản
- `last_login_at` (DATETIME): Lần đăng nhập gần nhất
- `deleted_at` (DATETIME): Ngày xóa mềm

Và tạo indexes cho tối ưu hóa query.

---

## Testing với cURL

### 1. Login để lấy token
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 2. Lấy danh sách tài khoản
```bash
curl -X GET http://localhost:8081/api/admin/accounts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Tìm kiếm tài khoản
```bash
curl -X GET "http://localhost:8081/api/admin/accounts/search?role=TRAINER&isActive=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Tạo tài khoản mới
```bash
curl -X POST http://localhost:8081/api/admin/accounts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newtrainer",
    "password": "password123",
    "role": "TRAINER",
    "email": "newtrainer@gym.com",
    "fullName": "New Trainer",
    "phone": "0912345678"
  }'
```

### 5. Cập nhật tài khoản
```bash
curl -X PUT http://localhost:8081/api/admin/accounts/5 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Updated Name",
    "phone": "0987654321"
  }'
```

### 6. Vô hiệu hóa tài khoản
```bash
curl -X PUT http://localhost:8081/api/admin/accounts/5/deactivate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 7. Xóa mềm tài khoản
```bash
curl -X DELETE http://localhost:8081/api/admin/accounts/5 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

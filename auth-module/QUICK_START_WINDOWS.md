# Quick Start Guide - Auth Module (Windows)

## ⚠️ Lỗi bạn đang gặp

Bạn đang chạy **monolith application** (trong `gym-management/gym-management/`) thay vì **auth-module** mới.

Chức năng quản lý tài khoản ADMIN được thêm vào **auth-module** riêng biệt.

---

## 🚀 Hướng dẫn chạy nhanh

### Bước 1: Setup Database

**Option A: Dùng MySQL Command Line**
```bash
mysql -u root -p < SETUP_DATABASE.sql
```

**Option B: Dùng MySQL Workbench**
1. Mở MySQL Workbench
2. File → Open SQL Script
3. Chọn file: `SETUP_DATABASE.sql`
4. Click Execute (⚡ icon)

### Bước 2: Cập nhật password database

Mở file: `backend/src/main/resources/application.properties`

Sửa dòng:
```properties
spring.datasource.password=your_mysql_password_here
```

Thành password MySQL của bạn:
```properties
spring.datasource.password=123456  # Ví dụ
```

### Bước 3: Chạy ứng dụng

**Cách 1: Double-click file START.bat**
```
Chạy file: START.bat
```

**Cách 2: Command Line**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Cách 3: IntelliJ IDEA**
1. Mở thư mục: `auth-module/backend` trong IntelliJ
2. Mở file: `src/main/java/dh13c8/nhom4/auth/AuthModuleApplication.java`
3. Chuột phải → Run 'AuthModuleApplication'

### Bước 4: Test

Ứng dụng chạy trên: **http://localhost:8081**

**Test Login:**
```bash
curl -X POST http://localhost:8081/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

Hoặc dùng Postman:
1. Import file: `postman_collection_admin_account_management.json`
2. Chạy request "1. Login"
3. Token sẽ tự động lưu vào biến
4. Test các endpoint khác

---

## 📌 Quan trọng

### Port khác nhau:
- **Monolith app** (cũ): `http://localhost:8080`
- **Auth Module** (mới): `http://localhost:8081`

### API Endpoints mới:
```
GET    /api/admin/accounts              - Danh sách tài khoản
GET    /api/admin/accounts/search       - Tìm kiếm & lọc
GET    /api/admin/accounts/{id}         - Chi tiết tài khoản
POST   /api/admin/accounts              - Tạo tài khoản
PUT    /api/admin/accounts/{id}         - Cập nhật
PUT    /api/admin/accounts/{id}/activate - Kích hoạt
PUT    /api/admin/accounts/{id}/deactivate - Vô hiệu hóa
DELETE /api/admin/accounts/{id}         - Xóa mềm
```

### Tài khoản mặc định:
- **Username:** admin
- **Password:** admin123

---

## 🔧 Khắc phục lỗi

### Lỗi: "UnsupportedClassVersionError"

**Nguyên nhân:** IDE đang dùng Java cũ

**Giải pháp:**

**IntelliJ IDEA:**
1. File → Project Structure (Ctrl+Alt+Shift+S)
2. Project Settings → Project
3. SDK: Chọn Java 21 hoặc cao hơn
4. Language Level: 21
5. Apply → OK

**Eclipse:**
1. Project → Properties
2. Java Compiler
3. Compiler compliance level: 21
4. Apply and Close

### Lỗi: "Could not connect to database"

**Giải pháp:**
1. Kiểm tra MySQL đang chạy
2. Kiểm tra password trong `application.properties`
3. Chạy script `SETUP_DATABASE.sql`

### Lỗi: "Port 8081 already in use"

**Giải pháp:**

**Option A: Đổi port**
Sửa `application.properties`:
```properties
server.port=8082
```

**Option B: Kill process đang dùng port 8081**
```bash
# Tìm process
netstat -ano | findstr :8081

# Kill process (thay PID)
taskkill /PID <PID> /F
```

---

## 📚 Tài liệu

- **API Documentation:** `API_ADMIN_ACCOUNT_MANAGEMENT.md`
- **Full README:** `ACCOUNT_MANAGEMENT_README.md`
- **Implementation Summary:** `../IMPLEMENTATION_SUMMARY.md`

---

## 💡 Tips

1. **Database tự động update schema** khi chạy lần đầu (nhờ `spring.jpa.hibernate.ddl-auto=update`)

2. **Logs chi tiết** được enable, xem console để debug

3. **Hot reload** đã enable với spring-boot-devtools - chỉnh code sẽ tự động reload

4. **Health check:** http://localhost:8081/actuator/health

---

**Need help?** Check logs trong console khi chạy ứng dụng.

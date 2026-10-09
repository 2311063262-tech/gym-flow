# Hướng dẫn chạy Auth Module

## Vấn đề hiện tại

Bạn đang cố chạy monolith application trong `gym-management/gym-management/` nhưng chức năng quản lý tài khoản mới được thêm vào `gym-management/auth-module/`.

## Giải pháp

### Bước 1: Cập nhật Database

Trước tiên cần chạy SQL script để thêm các cột mới vào bảng users:

```bash
# Mở MySQL và chạy:
mysql -u root -p auth_db < gym-management/sql/update_users_table_for_account_management.sql

# Hoặc trong MySQL Workbench:
# 1. Mở file: gym-management/sql/update_users_table_for_account_management.sql
# 2. Chọn database: auth_db
# 3. Execute script
```

### Bước 2: Cấu hình Database Connection

Mở file `gym-management/auth-module/backend/src/main/resources/application.properties` và cập nhật:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD_HERE

# JWT Configuration
jwt.secret=MySecretKeyForJWTTokenAuthenticationSystemGymManagement2026
jwt.expiration=86400000

# CORS Configuration
cors.allowed-origins=http://localhost:5501,http://127.0.0.1:5501
```

### Bước 3: Chạy Auth Module

**Option A: Chạy bằng Maven**
```bash
cd gym-management/auth-module/backend
mvn spring-boot:run
```

**Option B: Chạy bằng JAR file**
```bash
cd gym-management/auth-module/backend
mvn clean install
java -jar target/auth-module-1.0.0.jar
```

**Option C: Chạy trong IntelliJ IDEA**
1. Mở project: `gym-management/auth-module/backend`
2. Tìm file: `src/main/java/dh13c8/nhom4/auth/AuthModuleApplication.java`
3. Click chuột phải → Run 'AuthModuleApplication'

### Bước 4: Kiểm tra

Ứng dụng sẽ chạy trên port **8081**

Test bằng cURL:
```bash
# Test login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Nếu thành công, bạn sẽ nhận được JWT token.

## So sánh 2 ứng dụng

### Monolith Application (gym-management/gym-management/)
- **Port:** 8080
- **Có:** Các chức năng cũ
- **Không có:** Chức năng quản lý tài khoản mới

### Auth Module (gym-management/auth-module/)
- **Port:** 8081
- **Có:** Authentication + Quản lý tài khoản ADMIN mới
- **Endpoints mới:** `/api/admin/accounts/*`

## Khắc phục lỗi Java version

Nếu gặp lỗi về Java version khi chạy trong IDE:

1. **IntelliJ IDEA:**
   - File → Project Structure → Project
   - SDK: Chọn Java 21 hoặc cao hơn
   - Language Level: 21

2. **Eclipse:**
   - Project → Properties → Java Compiler
   - Compiler compliance level: 21

3. **Kiểm tra JAVA_HOME:**
   ```bash
   echo $JAVA_HOME  # Linux/Mac
   echo %JAVA_HOME%  # Windows
   
   # Nên trỏ đến Java 21+
   ```

## API Documentation

Xem chi tiết API tại:
- `gym-management/auth-module/API_ADMIN_ACCOUNT_MANAGEMENT.md`

## Postman Collection

Import file để test:
- `gym-management/auth-module/postman_collection_admin_account_management.json`

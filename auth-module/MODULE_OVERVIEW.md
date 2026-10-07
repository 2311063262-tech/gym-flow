# 📦 Auth Module Overview

## 🎯 Mục đích

Module này là **phiên bản tách riêng** của phần authentication trong Gym Management System, được **nâng cấp** với:
- ✅ **JWT Authentication** thay vì API Key
- ✅ **Spring Security** với phân quyền thực sự
- ✅ **BCrypt** password hashing thay vì plain text
- ✅ **Stateless** authentication
- ✅ **Production-ready** security

## 📊 So sánh với Main App

| Tính năng | Main App (`/`) | Auth Module (`/auth-module`) |
|-----------|----------------|------------------------------|
| **Port** | 8080 | 8081 |
| **Database** | `gym_db` (full) | `auth_db` (chỉ users) |
| **Authentication** | API Key trong header | JWT Token |
| **Authorization** | Query param `?role=ADMIN` | JWT payload + Spring Security |
| **Password** | Plain text `123456` | BCrypt hashed |
| **Login Response** | Trả về full User object (có password!) | Trả về JWT + UserResponse (KHÔNG có password) |
| **Protected Endpoints** | Kiểm tra `role` parameter | Kiểm tra JWT token + role trong payload |
| **Security** | Basic (API Key) | Advanced (Spring Security + JWT) |
| **Session** | Stateful (localStorage user) | Stateless (JWT token) |
| **CORS** | Port 5500 | Port 5501 |

## 📁 Files đã tách từ Main App

### ✅ Backend (Đã nâng cấp)

#### Entities
- [x] `User.java` - Giữ nguyên structure
- [x] `Role.java` - Enum ADMIN/TRAINER/MEMBER

#### Controllers
- [x] `AuthController.java` - **Nâng cấp**: Trả JWT thay vì User object
- [x] `UserController.java` - **Nâng cấp**: Xác thực bằng JWT

#### Services
- [x] `AuthService.java` - **Nâng cấp**: Tích hợp Spring Security
- [x] `UserService.java` - **Nâng cấp**: Check role qua SecurityContext

#### Repositories
- [x] `UserRepository.java` - Giữ nguyên

#### ⭐ Security (MỚI - Không có trong main app)
- [x] `JwtService.java` - Tạo và validate JWT token
- [x] `JwtAuthenticationFilter.java` - Filter mỗi request
- [x] `SecurityConfig.java` - Spring Security configuration
- [x] `CustomUserDetailsService.java` - Load user cho Spring Security

#### ⭐ DTOs (MỚI - Main app trả raw entity)
- [x] `LoginRequest.java` - Request body cho login
- [x] `AuthResponse.java` - Response với token + user (no password)
- [x] `UserResponse.java` - User DTO không có password

### ✅ Frontend (Đã nâng cấp)

- [x] `index.html` - **Nâng cấp**: Hiển thị JWT token
- [x] `login.js` - **Nâng cấp**: Lưu JWT token, hiển thị để test
- [x] `common.js` - **Nâng cấp**: Gửi `Authorization: Bearer <token>`

### ✅ Database

- [x] `auth_db.sql` - **Nâng cấp**: Password đã hash BCrypt
  - Main app: `password = '123456'` (plain text)
  - Auth module: `password = '$2a$10$xqN7...'` (BCrypt)

### ✅ Configuration

- [x] `pom.xml` - **Thêm**: Spring Security, jjwt dependencies
- [x] `application.properties` - **Thêm**: JWT secret, expiration, CORS

### ✅ Documentation

- [x] `README.md` - Full documentation
- [x] `QUICK_START.md` - Hướng dẫn nhanh
- [x] `MODULE_OVERVIEW.md` - File này
- [x] `.gitignore`

## 🚫 Không tách (Không cần thiết cho authentication)

Main app có nhưng auth module KHÔNG cần:
- ❌ `Member.java`, `Trainer.java` - Chỉ cần User
- ❌ `GymPackage.java`, `Registration.java` - Business logic
- ❌ `ClassSession.java`, `ClassRegistration.java` - Business logic
- ❌ `Equipment.java` - Business logic
- ❌ Dashboard/Member/Trainer pages - Chưa implement

## 🔄 Migration Path

Để migrate main app sang JWT:

### 1. Update Main App Backend
```java
// Thay đổi trong main app:
// 1. Thêm dependencies từ auth-module/pom.xml
// 2. Copy toàn bộ folder security/
// 3. Update AuthController trả JWT
// 4. Update các Controller khác xác thực qua JWT
// 5. Hash password bằng BCrypt
```

### 2. Update Main App Frontend
```javascript
// Thay đổi trong client/:
// 1. Update common.js gửi Bearer token
// 2. Update login.js nhận và lưu JWT
// 3. Xóa API_KEY constant
```

### 3. Update Database
```sql
-- Hash lại tất cả password
UPDATE users SET password = 
  BCrypt_hash('123456');  -- Dùng Java code
```

## 📚 Learning Points

### Main App (Current)
```java
// AuthController
@PostMapping("/login")
public User login(@RequestBody User user) {
    return authService.login(user.getUsername(), user.getPassword())
        .orElse(null);  // ❌ Trả full User, có cả password!
}

// UserController
@GetMapping
public List<User> getAll(@RequestParam String role) {
    userService.checkRole(role, "ADMIN");  // ❌ Role từ param
    return userRepository.findAll();
}
```

### Auth Module (JWT)
```java
// AuthController
@PostMapping("/login")
public AuthResponse login(@RequestBody LoginRequest req) {
    // ✅ Authenticate với Spring Security
    // ✅ Tạo JWT token
    // ✅ Trả AuthResponse (token + user NO password)
}

// UserController  
@GetMapping
public List<UserResponse> getAll() {
    // ✅ Role từ JWT token trong SecurityContext
    userService.checkRole(Role.ADMIN);
    return userService.getAllUsers();
}
```

## 🎓 Khuyến nghị

### Dùng Auth Module khi:
- ✅ Học JWT authentication
- ✅ Cần security thực sự
- ✅ Deploy production
- ✅ Multiple frontend clients (web, mobile)
- ✅ Microservices architecture

### Dùng Main App khi:
- ✅ Demo nhanh
- ✅ Học CRUD operations
- ✅ Prototype
- ✅ Không cần security phức tạp

## 🔗 Links

- **Main App**: `http://localhost:8080` (port 8080)
- **Auth Module**: `http://localhost:8081` (port 8081)
- **Main App Frontend**: `http://127.0.0.1:5500`
- **Auth Module Frontend**: `http://127.0.0.1:5501`

Hai module có thể **chạy song song** vì dùng:
- Database khác nhau (`gym_db` vs `auth_db`)
- Port khác nhau (8080 vs 8081)
- Frontend port khác nhau (5500 vs 5501)

## ✨ Bonus: JWT Token Example

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY3ODg4ODg4OCwiZXhwIjoxNjc4OTc1Mjg4fQ.
Xk3p8Y7Z9Q_signature_here
```

Decode tại https://jwt.io:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
{
  "sub": "admin",
  "iat": 1678888888,
  "exp": 1678975288
}
```

---

🎉 **Auth Module hoàn chỉnh và sẵn sàng sử dụng!**

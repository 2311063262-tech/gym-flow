# ⚡ QUICK START - Auth Module với JWT

## 🎯 3 bước chạy Auth Module:

### Bước 1️⃣: Import Database
```bash
mysql -u root -p < sql/auth_db.sql
```
✅ Database `auth_db` được tạo với 5 users (password đã hash BCrypt)

### Bước 2️⃣: Chạy Backend
```bash
cd backend
mvn spring-boot:run
```
Hoặc dùng IntelliJ: Run `AuthModuleApplication.java`

✅ Backend: http://localhost:8081

### Bước 3️⃣: Chạy Frontend
```bash
cd frontend
python -m http.server 5501
```
Hoặc VS Code Live Server ở port 5501

✅ Frontend: http://127.0.0.1:5501/index.html

---

## 🔑 Test Login:

```
Username: admin
Password: 123456
```

Sau khi login, JWT token sẽ hiển thị trên màn hình! 🎉

---

## 📝 So sánh với Main App:

| Feature | Main App (port 8080) | Auth Module (port 8081) |
|---------|---------------------|------------------------|
| Authentication | API Key trong header | JWT Token |
| Authorization | Query param `?role=` | JWT payload + Spring Security |
| Password | Plain text | BCrypt hashed |
| Session | Stateful | Stateless |
| Security | Basic | Spring Security |

---

## 🧪 Test JWT với Postman:

### 1. Login
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

### 2. Copy token từ response

### 3. Get Users (Protected)
```http
GET http://localhost:8081/api/users
Authorization: Bearer <paste_token_here>
```

---

## 🔐 JWT Token Format:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

- **Header**: Algorithm (HS256) + Type (JWT)
- **Payload**: Username + Issued At + Expiration
- **Signature**: HMAC-SHA256

Paste token vào https://jwt.io để decode!

---

## 💡 Key Differences:

### Main App (Old way):
```javascript
// Login response
{
  "id": 1,
  "username": "admin",
  "password": "123456",  // ❌ Password exposed!
  "role": "ADMIN"
}

// Request
fetch("/api2025/users?role=ADMIN", {
  headers: {
    "X-API-KEY": "SECRET_KEY_123"
  }
})
```

### Auth Module (JWT way):
```javascript
// Login response
{
  "token": "eyJhbGci...",  // ✅ JWT token
  "user": {
    "username": "admin",
    // ✅ NO password field!
    "role": "ADMIN"
  }
}

// Request
fetch("/api/users", {
  headers: {
    "Authorization": "Bearer " + token  // ✅ JWT
  }
})
```

---

## ✅ Checklist:

- [x] MySQL running
- [x] Database `auth_db` imported
- [x] Backend running port 8081
- [x] Frontend running port 5501
- [x] Can login với admin/123456
- [x] JWT token displayed
- [x] Can test với Postman

🎉 **Auth Module Ready!**

---

## 📚 Đọc thêm:

- Full documentation: `README.md`
- API endpoints: `README.md#api-documentation`
- Security features: `README.md#security`

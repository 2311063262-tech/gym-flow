-- Setup script cho Auth Module Database
-- Chạy script này trước khi start application

-- Tạo database (nếu chưa có)
CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

-- Tạo bảng users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TRAINER', 'MEMBER') NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_login_at DATETIME(6) NULL,
    deleted_at DATETIME(6) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo indexes
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Tạo tài khoản admin mặc định (password: admin123)
INSERT INTO users (username, password, role, full_name, email, phone, is_active, created_at)
VALUES 
('admin', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'ADMIN', 'Administrator', 'admin@gym.com', '0900000000', TRUE, NOW())
ON DUPLICATE KEY UPDATE username=username;

-- Hiển thị kết quả
SELECT 'Database setup completed!' as Status;
SELECT * FROM users WHERE role = 'ADMIN';

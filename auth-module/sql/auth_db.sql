-- Database cho Auth Module
CREATE DATABASE IF NOT EXISTS auth_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE auth_db;
SET NAMES utf8mb4;

-- Xóa bảng nếu tồn tại
DROP TABLE IF EXISTS users;

-- Tạo bảng users
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'TRAINER', 'MEMBER') NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20)
);

-- Insert dữ liệu mẫu
-- Lưu ý: Password đã được hash bằng BCrypt
-- Plain password: 123456
-- BCrypt hash for demo password 123456.

INSERT INTO users (username, password, role, full_name, email, phone) VALUES
('admin', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'ADMIN', 'Nguyễn Văn Admin', 'admin@gym.local', '0901000001'),
('trainer1', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'TRAINER', 'Trần Minh Huấn', 'trainer1@gym.local', '0901000002'),
('trainer2', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'TRAINER', 'Lê Thị Yoga', 'trainer2@gym.local', '0901000003'),
('member1', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'MEMBER', 'Phạm Quốc Hội', 'member1@gym.local', '0901000004'),
('member2', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'MEMBER', 'Đỗ Thị Lan', 'member2@gym.local', '0901000005');

-- Lưu ý: Để tạo password hash mới, có thể dùng online tool hoặc code Java:
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- String hash = encoder.encode("your_password");

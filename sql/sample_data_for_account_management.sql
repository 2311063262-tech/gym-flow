-- Sample data để test chức năng quản lý tài khoản
-- Chạy sau khi đã chạy update_users_table_for_account_management.sql

USE auth_db;

-- Tạo tài khoản admin (nếu chưa có)
INSERT INTO users (username, password, role, full_name, email, phone, is_active, created_at)
VALUES 
('admin', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'ADMIN', 'Administrator', 'admin@gym.com', '0900000000', TRUE, '2024-01-01 00:00:00')
ON DUPLICATE KEY UPDATE username=username;
-- Password: admin123

-- Tạo một số tài khoản TRAINER
INSERT INTO users (username, password, role, full_name, email, phone, is_active, created_at, last_login_at)
VALUES 
('trainer01', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'TRAINER', 'Nguyễn Văn A', 'trainer01@gym.com', '0901234567', TRUE, '2024-01-15 10:30:00', '2024-02-20 14:25:00'),
('trainer02', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'TRAINER', 'Trần Văn B', 'trainer02@gym.com', '0902345678', TRUE, '2024-01-16 09:00:00', '2024-02-21 08:15:00'),
('trainer03', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'TRAINER', 'Lê Thị C', 'trainer03@gym.com', '0903456789', FALSE, '2024-01-20 11:00:00', NULL)
ON DUPLICATE KEY UPDATE username=username;
-- Password tất cả: 123456

-- Tạo một số tài khoản MEMBER
INSERT INTO users (username, password, role, full_name, email, phone, is_active, created_at, last_login_at)
VALUES 
('member01', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'MEMBER', 'Phạm Văn D', 'member01@gmail.com', '0909876543', TRUE, '2024-01-20 09:15:00', '2024-02-21 08:00:00'),
('member02', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'MEMBER', 'Hoàng Thị E', 'member02@gmail.com', '0908765432', TRUE, '2024-01-22 14:20:00', '2024-02-20 19:30:00'),
('member03', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'MEMBER', 'Vũ Văn F', 'member03@gmail.com', '0907654321', TRUE, '2024-01-25 16:45:00', NULL),
('member04', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'MEMBER', 'Đỗ Thị G', 'member04@gmail.com', '0906543210', FALSE, '2024-02-01 10:00:00', '2024-02-15 12:00:00')
ON DUPLICATE KEY UPDATE username=username;
-- Password tất cả: 123456

-- Tạo tài khoản đã bị xóa mềm (để test)
INSERT INTO users (username, password, role, full_name, email, phone, is_active, created_at, deleted_at)
VALUES 
('deleted_user', '$2a$10$LgmHvukl1ESKIEkJTRQepOcL7YY27tVQGIkiXKSSllpdCy/UeuNuy', 'MEMBER', 'Đã Xóa User', 'deleted@gmail.com', '0905432109', FALSE, '2024-01-10 08:00:00', '2024-02-10 15:30:00')
ON DUPLICATE KEY UPDATE username=username;

-- Hiển thị thống kê
SELECT 
    'Tổng số tài khoản' as 'Loại',
    COUNT(*) as 'Số lượng'
FROM users
UNION ALL
SELECT 
    'Tài khoản ADMIN',
    COUNT(*)
FROM users WHERE role = 'ADMIN'
UNION ALL
SELECT 
    'Tài khoản TRAINER',
    COUNT(*)
FROM users WHERE role = 'TRAINER' AND deleted_at IS NULL
UNION ALL
SELECT 
    'Tài khoản MEMBER',
    COUNT(*)
FROM users WHERE role = 'MEMBER' AND deleted_at IS NULL
UNION ALL
SELECT 
    'Tài khoản đang hoạt động',
    COUNT(*)
FROM users WHERE is_active = TRUE AND deleted_at IS NULL
UNION ALL
SELECT 
    'Tài khoản bị vô hiệu hóa',
    COUNT(*)
FROM users WHERE is_active = FALSE AND deleted_at IS NULL
UNION ALL
SELECT 
    'Tài khoản đã xóa mềm',
    COUNT(*)
FROM users WHERE deleted_at IS NOT NULL;

-- Hiển thị danh sách tài khoản
SELECT 
    id,
    username,
    role,
    full_name,
    email,
    phone,
    is_active,
    created_at,
    last_login_at,
    deleted_at
FROM users
ORDER BY created_at DESC;

-- Thông tin đăng nhập để test:
-- Admin: username=admin, password=admin123
-- Trainer: username=trainer01, password=123456
-- Member: username=member01, password=123456

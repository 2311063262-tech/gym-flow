-- Script cập nhật bảng users cho chức năng quản lý tài khoản
-- Chạy script này trên database auth_db

USE auth_db;

-- Kiểm tra và thêm cột is_active nếu chưa có
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'auth_db' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'is_active'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER phone', 
    'SELECT "Column is_active already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và thêm cột created_at nếu chưa có
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'auth_db' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'created_at'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE users ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER is_active', 
    'SELECT "Column created_at already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và thêm cột last_login_at nếu chưa có
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'auth_db' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'last_login_at'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE users ADD COLUMN last_login_at DATETIME(6) NULL AFTER created_at', 
    'SELECT "Column last_login_at already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Kiểm tra và thêm cột deleted_at nếu chưa có
SET @column_exists = (
    SELECT COUNT(*) 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'auth_db' 
    AND TABLE_NAME = 'users' 
    AND COLUMN_NAME = 'deleted_at'
);

SET @sql = IF(@column_exists = 0, 
    'ALTER TABLE users ADD COLUMN deleted_at DATETIME(6) NULL AFTER last_login_at', 
    'SELECT "Column deleted_at already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Tạo indexes nếu chưa có
CREATE INDEX IF NOT EXISTS idx_users_deleted_at ON users(deleted_at);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Hiển thị cấu trúc bảng sau khi cập nhật
DESCRIBE users;

-- Hiển thị số lượng records hiện tại
SELECT 
    COUNT(*) as total_users,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) as active_users,
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) as inactive_users,
    SUM(CASE WHEN deleted_at IS NOT NULL THEN 1 ELSE 0 END) as deleted_users
FROM users;

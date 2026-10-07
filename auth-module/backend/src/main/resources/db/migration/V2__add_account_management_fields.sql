-- Migration script: Thêm các trường cho chức năng quản lý tài khoản
-- Thêm vào bảng users: is_active, created_at, last_login_at, deleted_at

-- Thêm cột is_active (mặc định TRUE cho các tài khoản hiện có)
ALTER TABLE users 
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER phone;

-- Thêm cột created_at (sử dụng timestamp hiện tại cho các records cũ)
ALTER TABLE users 
ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER is_active;

-- Thêm cột last_login_at (NULL cho các tài khoản chưa đăng nhập)
ALTER TABLE users 
ADD COLUMN last_login_at DATETIME(6) NULL AFTER created_at;

-- Thêm cột deleted_at (NULL cho các tài khoản chưa bị xóa)
ALTER TABLE users 
ADD COLUMN deleted_at DATETIME(6) NULL AFTER last_login_at;

-- Tạo index cho việc query tài khoản chưa bị xóa
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- Tạo index cho việc query theo is_active
CREATE INDEX idx_users_is_active ON users(is_active);

-- Tạo index cho việc query theo role
CREATE INDEX idx_users_role ON users(role);

-- Tạo index cho email (để kiểm tra trùng lặp nhanh hơn)
CREATE INDEX idx_users_email ON users(email);

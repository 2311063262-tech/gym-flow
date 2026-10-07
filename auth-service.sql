CREATE DATABASE IF NOT EXISTS auth_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE auth_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    `role` VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

SET @has_role_column = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role'
);

SET @sql = IF(
    @has_role_column = 0,
    'ALTER TABLE `users` ADD COLUMN `role` VARCHAR(20) NULL',
    'SELECT 1'
);
PREPARE role_column_statement FROM @sql;
EXECUTE role_column_statement;
DEALLOCATE PREPARE role_column_statement;

SET @has_created_at_column = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'created_at'
);

SET @sql = IF(
    @has_created_at_column = 0,
    'ALTER TABLE `users` ADD COLUMN `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)',
    'SELECT 1'
);
PREPARE created_at_column_statement FROM @sql;
EXECUTE created_at_column_statement;
DEALLOCATE PREPARE created_at_column_statement;

SET @schema_columns = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'full_name'
);
SET @sql = IF(@schema_columns = 0,
    'ALTER TABLE `users` ADD COLUMN `full_name` VARCHAR(100) NULL', 'SELECT 1');
PREPARE full_name_column_statement FROM @sql;
EXECUTE full_name_column_statement;
DEALLOCATE PREPARE full_name_column_statement;

SET @schema_columns = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'phone'
);
SET @sql = IF(@schema_columns = 0,
    'ALTER TABLE `users` ADD COLUMN `phone` VARCHAR(30) NULL', 'SELECT 1');
PREPARE phone_column_statement FROM @sql;
EXECUTE phone_column_statement;
DEALLOCATE PREPARE phone_column_statement;

SET @schema_columns = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'is_active'
);
SET @sql = IF(@schema_columns = 0,
    'ALTER TABLE `users` ADD COLUMN `is_active` BOOLEAN NOT NULL DEFAULT TRUE', 'SELECT 1');
PREPARE is_active_column_statement FROM @sql;
EXECUTE is_active_column_statement;
DEALLOCATE PREPARE is_active_column_statement;

SET @schema_columns = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'updated_at'
);
SET @sql = IF(@schema_columns = 0,
    'ALTER TABLE `users` ADD COLUMN `updated_at` DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)',
    'SELECT 1');
PREPARE updated_at_column_statement FROM @sql;
EXECUTE updated_at_column_statement;
DEALLOCATE PREPARE updated_at_column_statement;

SET @schema_columns = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'deleted_at'
);
SET @sql = IF(@schema_columns = 0,
    'ALTER TABLE `users` ADD COLUMN `deleted_at` DATETIME(6) NULL', 'SELECT 1');
PREPARE deleted_at_column_statement FROM @sql;
EXECUTE deleted_at_column_statement;
DEALLOCATE PREPARE deleted_at_column_statement;

SET @schema_columns = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'last_login_at'
);
SET @sql = IF(@schema_columns = 0,
    'ALTER TABLE `users` ADD COLUMN `last_login_at` DATETIME(6) NULL', 'SELECT 1');
PREPARE last_login_column_statement FROM @sql;
EXECUTE last_login_column_statement;
DEALLOCATE PREPARE last_login_column_statement;

SET @has_legacy_role_id = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role_id'
);

SET @has_roles_table = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'roles'
);

SET @sql = IF(
    @has_legacy_role_id > 0 AND @has_roles_table > 0,
    'UPDATE `users` u LEFT JOIN `roles` r ON r.id = u.role_id SET u.`role` = r.role_name WHERE u.id > 0 AND u.`role` IS NULL',
    'SELECT 1'
);
PREPARE copy_legacy_roles_statement FROM @sql;
EXECUTE copy_legacy_roles_statement;
DEALLOCATE PREPARE copy_legacy_roles_statement;

UPDATE `users`
SET `role` = 'MEMBER'
WHERE `id` > 0
  AND (`role` IS NULL
   OR `role` = ''
   OR `role` NOT IN ('ADMIN', 'STAFF', 'TRAINER', 'MEMBER'));

ALTER TABLE `users`
    MODIFY COLUMN `role` VARCHAR(20) NOT NULL;

SET @legacy_role_foreign_key = (
    SELECT CONSTRAINT_NAME
    FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'users'
      AND COLUMN_NAME = 'role_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);

SET @sql = IF(
    @legacy_role_foreign_key IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE `users` DROP FOREIGN KEY `', REPLACE(@legacy_role_foreign_key, '`', '``'), '`')
);
PREPARE legacy_role_foreign_key_statement FROM @sql;
EXECUTE legacy_role_foreign_key_statement;
DEALLOCATE PREPARE legacy_role_foreign_key_statement;

SET @sql = IF(
    @has_legacy_role_id > 0,
    'ALTER TABLE `users` DROP COLUMN `role_id`',
    'SELECT 1'
);
PREPARE legacy_role_id_column_statement FROM @sql;
EXECUTE legacy_role_id_column_statement;
DEALLOCATE PREPARE legacy_role_id_column_statement;

INSERT IGNORE INTO `users` (`username`, `email`, `password`, `role`, `created_at`) VALUES
    ('admin', 'admin@gymflow.com', '$2a$10$ENwmMfLgVHjQnrFnCSN.Xuvk1g6dyy6RKjCAHbyX4kNGebhPJjnK6', 'ADMIN', CURRENT_TIMESTAMP(6)),
    ('trainer1', 'trainer1@gymflow.com', '$2a$10$SWZNRf1xVu6kdHiKWN.JIeIEkUXZVTm7jg5wdx2deeKaUwD./xPv.', 'TRAINER', CURRENT_TIMESTAMP(6)),
    ('member1', 'member1@gymflow.com', '$2a$10$K.BRPlyOpYuYxaeFK.F/8u4xJBddo5qBSMIhq3Uv1I14tvSixm3w.', 'MEMBER', CURRENT_TIMESTAMP(6));

CREATE TABLE IF NOT EXISTS revoked_tokens (
    jti VARCHAR(36) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (jti),
    KEY idx_revoked_tokens_expires_at (expires_at)
) ENGINE=InnoDB
  DEFAULT CHARACTER SET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

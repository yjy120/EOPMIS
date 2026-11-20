-- EOPMIS init SQL (ASCII-only comments for cross-platform safety)
DROP DATABASE IF EXISTS eopmis;
CREATE DATABASE eopmis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE eopmis;

-- sys_role
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    code         VARCHAR(32)  NOT NULL,
    name         VARCHAR(32)  NOT NULL,
    description  VARCHAR(128) DEFAULT NULL,
    is_system    TINYINT      NOT NULL DEFAULT 0,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sys_user
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(32)  NOT NULL,
    real_name       VARCHAR(32)  NOT NULL,
    password        VARCHAR(128) NOT NULL,
    status          TINYINT      NOT NULL DEFAULT 1,
    role_id         BIGINT       NOT NULL,
    employee_id     BIGINT       DEFAULT NULL,
    fail_count      INT          NOT NULL DEFAULT 0,
    locked_until    DATETIME     DEFAULT NULL,
    last_login_at   DATETIME     DEFAULT NULL,
    last_login_ip   VARCHAR(64)  DEFAULT NULL,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT       DEFAULT NULL,
    update_by       BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sys_menu
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id    BIGINT       NOT NULL DEFAULT 0,
    name         VARCHAR(32)  NOT NULL,
    perm         VARCHAR(64)  DEFAULT NULL,
    type         TINYINT      NOT NULL DEFAULT 2,
    path         VARCHAR(128) DEFAULT NULL,
    icon         VARCHAR(32)  DEFAULT NULL,
    sort         INT          NOT NULL DEFAULT 0,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sys_role_menu
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id      BIGINT   NOT NULL,
    menu_id      BIGINT   NOT NULL,
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, menu_id),
    KEY idx_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sys_password_history
DROP TABLE IF EXISTS sys_password_history;
CREATE TABLE sys_password_history (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    password     VARCHAR(128) NOT NULL,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_time (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- sys_audit_log
DROP TABLE IF EXISTS sys_audit_log;
CREATE TABLE sys_audit_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       DEFAULT NULL,
    username     VARCHAR(32)  DEFAULT NULL,
    module       VARCHAR(32)  DEFAULT NULL,
    action       VARCHAR(32)  DEFAULT NULL,
    detail       VARCHAR(255) DEFAULT NULL,
    ip           VARCHAR(64)  DEFAULT NULL,
    result       TINYINT      DEFAULT 1,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_time (user_id, create_time),
    KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- seed roles
INSERT INTO sys_role (id, code, name, description, is_system) VALUES
(1, 'ADMIN',    'Sys Admin',   'admin',       1),
(2, 'HR',       'HR Officer',  'hr',          1),
(3, 'EMPLOYEE', 'Employee',    'employee',    1);

-- seed menus (English names; DataInitRunner will set them in Java side)
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(1,  0, 'System',     NULL,                  1, '/sys',     'setting',  100),
(11, 1, 'Accounts',   'sys:user:view',       2, '/sys/user',  NULL,    1),
(12, 1, 'Roles',      'sys:role:view',       2, '/sys/role',  NULL,    2),
(13, 1, 'Audit',      'sys:audit:view',      2, '/sys/audit', NULL,    3),
(2,  0, 'My Center',  NULL,                  1, '/me',      'user',     50),
(21, 2, 'My Profile', 'me:profile:view',     2, '/me/profile', NULL,   1),
(22, 2, 'Change Pwd', 'me:pwd:update',       2, '/me/pwd',     NULL,   2),
(3,  0, 'HR',         NULL,                  1, '/hr',      'team',     10),
(1101, 11, 'Add User',  'sys:user:add',     3, NULL, NULL, 1),
(1102, 11, 'Enable',    'sys:user:enable',  3, NULL, NULL, 2),
(1103, 11, 'Reset Pwd', 'sys:user:reset',   3, NULL, NULL, 3),
(1201, 12, 'Edit Role', 'sys:role:edit',    3, NULL, NULL, 1),
(1202, 12, 'Assign',    'sys:role:assign',  3, NULL, NULL, 2);

-- role-menu mapping
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 3);
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (3, 2), (3, 21), (3, 22);

-- seed users with placeholder BCrypt (DataInitRunner will rewrite on backend startup)
INSERT INTO sys_user (username, real_name, password, status, role_id, fail_count, create_time, update_time) VALUES
('admin', 'Sys Admin',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, 0, NOW(), NOW()),
('hr01',  'HR Officer', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 2, 0, NOW(), NOW()),
('emp01', 'Employee 1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 3, 0, NOW(), NOW());

SELECT 'roles' AS t, COUNT(*) AS cnt FROM sys_role
UNION ALL SELECT 'menus', COUNT(*) FROM sys_menu
UNION ALL SELECT 'role_menus', COUNT(*) FROM sys_role_menu
UNION ALL SELECT 'users', COUNT(*) FROM sys_user;

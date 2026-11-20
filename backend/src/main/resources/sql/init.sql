-- ============================================================
-- EOPMIS 4 个用例的最小初始化脚本
--   覆盖：UC-001 登录、UC-002 改密、UC-003 账号管理、UC-004 角色权限
--   仅含 sys_user / sys_role / sys_menu / sys_role_menu / sys_password_history / sys_audit_log
--   其余业务表按 docs/数据库设计文档.md 后续补齐
-- ============================================================

CREATE DATABASE IF NOT EXISTS eopmis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE eopmis;

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    code         VARCHAR(32)  NOT NULL COMMENT '角色编码 ADMIN/HR/EMPLOYEE',
    name         VARCHAR(32)  NOT NULL COMMENT '角色名称',
    description  VARCHAR(128) DEFAULT NULL,
    is_system    TINYINT      NOT NULL DEFAULT 0 COMMENT '1=系统内置',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

-- ----------------------------
-- 用户账号
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(32)  NOT NULL COMMENT '登录账号',
    real_name       VARCHAR(32)  NOT NULL COMMENT '真实姓名',
    password        VARCHAR(128) NOT NULL COMMENT 'BCrypt 密文',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0=停用 1=正常',
    role_id         BIGINT       NOT NULL COMMENT '角色ID',
    employee_id     BIGINT       DEFAULT NULL COMMENT '绑定员工ID（普通职工）',
    fail_count      INT          NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until    DATETIME     DEFAULT NULL COMMENT '锁定截止时间',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户账号';

-- ----------------------------
-- 菜单/权限点
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id    BIGINT       NOT NULL DEFAULT 0,
    name         VARCHAR(32)  NOT NULL,
    perm         VARCHAR(64)  DEFAULT NULL COMMENT '权限标识 @PreAuthorize',
    type         TINYINT      NOT NULL DEFAULT 2 COMMENT '1=目录 2=菜单 3=按钮',
    path         VARCHAR(128) DEFAULT NULL,
    icon         VARCHAR(32)  DEFAULT NULL,
    sort         INT          NOT NULL DEFAULT 0,
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单/权限点';

-- ----------------------------
-- 角色-菜单
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id      BIGINT   NOT NULL,
    menu_id      BIGINT   NOT NULL,
    create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, menu_id),
    KEY idx_menu (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单关联';

-- ----------------------------
-- 密码历史
-- ----------------------------
DROP TABLE IF EXISTS sys_password_history;
CREATE TABLE sys_password_history (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    password     VARCHAR(128) NOT NULL COMMENT 'BCrypt 密文',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_time (user_id, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密码历史（仅用于"不能与最近3次相同"）';

-- ----------------------------
-- 审计日志
-- ----------------------------
DROP TABLE IF EXISTS sys_audit_log;
CREATE TABLE sys_audit_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       DEFAULT NULL,
    username     VARCHAR(32)  DEFAULT NULL,
    module       VARCHAR(32)  DEFAULT NULL,
    action       VARCHAR(32)  DEFAULT NULL,
    detail       VARCHAR(255) DEFAULT NULL,
    ip           VARCHAR(64)  DEFAULT NULL,
    result       TINYINT      DEFAULT 1 COMMENT '1=成功 0=失败',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_time (user_id, create_time),
    KEY idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';

-- ============================================================
-- 种子数据
-- ============================================================
INSERT INTO sys_role (id, code, name, description, is_system) VALUES
(1, 'ADMIN',    '系统管理员', '管账号、角色、字典、审计', 1),
(2, 'HR',       '人事专员',   '人事业务数据全权限',        1),
(3, 'EMPLOYEE', '普通职工',   '只读本人数据 + 改密',       1);

-- 菜单/权限点（覆盖 4 个用例所需的全部权限）
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(1,  0, '系统管理',         NULL,                  1, '/sys',     'setting',  100),
(11, 1, '账号管理',         'sys:user:view',       2, '/sys/user',  NULL,    1),
(12, 1, '角色管理',         'sys:role:view',       2, '/sys/role',  NULL,    2),
(13, 1, '审计日志',         'sys:audit:view',      2, '/sys/audit', NULL,    3),

(2,  0, '个人中心',         NULL,                  1, '/me',      'user',     50),
(21, 2, '我的资料',         'me:profile:view',     2, '/me/profile', NULL,   1),
(22, 2, '修改密码',         'me:pwd:update',       2, '/me/pwd',     NULL,   2),

(3,  0, '人事业务',         NULL,                  1, '/hr',      'team',     10);

-- 按钮/接口权限点
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(1101, 11, '新增账号',  'sys:user:add',     3, NULL, NULL, 1),
(1102, 11, '启停账号',  'sys:user:enable',  3, NULL, NULL, 2),
(1103, 11, '重置密码',  'sys:user:reset',   3, NULL, NULL, 3),
(1201, 12, '编辑角色',  'sys:role:edit',    3, NULL, NULL, 1),
(1202, 12, '分配权限',  'sys:role:assign',  3, NULL, NULL, 2);

-- 角色权限（种子默认）
-- ADMIN: 全部
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- HR: 仅人事业务目录
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 3);

-- EMPLOYEE: 个人中心
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 2), (3, 21), (3, 22);

-- 默认账号（密码均为 BCrypt 密文）
--   admin / YOUR_ADMIN_PWD
--   hr01  / Hr@YOUR_DB_PASSWORD
--   emp01 / Emp@YOUR_DB_PASSWORD
-- BCrypt（cost=10）由 PasswordUtil.hash() 生成，SQL 中为占位示例
-- 实际使用时执行下列步骤生成密文：
--   1) 启动项目后，调用任意"重置密码"接口重置即可
--   或
--   2) 在 Java 端执行 BCrypt.hashpw("YOUR_ADMIN_PWD", BCrypt.gensalt(10)) 写入
-- 下面是常见的测试密文样例（不同 BCrypt 版本前缀不同，BCrypt 默认 $2a$10$）：
INSERT INTO sys_user (username, real_name, password, status, role_id, fail_count, create_time, update_time) VALUES
('admin', '系统管理员', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, 0, NOW(), NOW()),
('hr01',  '人事专员A',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 2, 0, NOW(), NOW()),
('emp01', '普通员工1',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 3, 0, NOW(), NOW());

-- 启动项目后，必须用登录接口先把这些账号的密码改写为正确 BCrypt，
-- 或者直接用 BCryptPasswordEncoder.matches("YOUR_ADMIN_PWD", "<上面密文>") 验证
-- 若测试登录失败，请使用 README 中"重置 admin 密码"小节的 SQL 命令

-- ============================================================
-- 验证
-- ============================================================
SELECT 'roles' AS t, COUNT(*) FROM sys_role
UNION ALL SELECT 'menus', COUNT(*) FROM sys_menu
UNION ALL SELECT 'role_menus', COUNT(*) FROM sys_role_menu
UNION ALL SELECT 'users', COUNT(*) FROM sys_user;

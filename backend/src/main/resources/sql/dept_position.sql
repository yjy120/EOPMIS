-- ============================================================
-- EOPMIS UC-005 部门管理 + UC-006 岗位管理 建表脚本
--   仅新增 sys_dept / sys_position 两张业务表
--   仅追加 sys_menu / sys_role_menu 行（不动 init.sql 既有数据）
--   追加 sys_dept 演示种子数据
--
-- 执行前提：已执行过 init.sql（3 角色、13 菜单、3 测试账号）
-- 执行顺序：在 init.sql 之后执行本脚本
-- ============================================================

USE eopmis;

-- ----------------------------
-- 部门表（自引用树形）
-- ----------------------------
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    parent_id       BIGINT       NOT NULL DEFAULT 0 COMMENT '上级部门ID，0=顶级',
    name            VARCHAR(64)  NOT NULL COMMENT '部门名称',
    code            VARCHAR(32)  DEFAULT NULL COMMENT '部门编码（可空）',
    leader          VARCHAR(32)  DEFAULT NULL COMMENT '负责人',
    sort            INT          NOT NULL DEFAULT 0,
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0=停用 1=正常',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT       DEFAULT NULL,
    update_by       BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_dept_parent (parent_id),
    KEY idx_dept_status (status),
    KEY idx_dept_parent_name (parent_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门';

-- ----------------------------
-- 岗位表（必须属于某个部门）
-- ----------------------------
DROP TABLE IF EXISTS sys_position;
CREATE TABLE sys_position (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    dept_id         BIGINT       NOT NULL COMMENT '所属部门ID',
    name            VARCHAR(64)  NOT NULL COMMENT '岗位名称',
    code            VARCHAR(32)  DEFAULT NULL COMMENT '岗位编码',
    description     VARCHAR(255) DEFAULT NULL COMMENT '岗位职责',
    level           VARCHAR(16)  DEFAULT NULL COMMENT '职级 P1/P2/...',
    sort            INT          NOT NULL DEFAULT 0,
    status          TINYINT      NOT NULL DEFAULT 1,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT       DEFAULT NULL,
    update_by       BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_pos_dept (dept_id),
    KEY idx_pos_status (status),
    KEY idx_pos_dept_name (dept_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位';

-- ============================================================
-- 菜单与权限点（追加 2 个菜单 + 6 个按钮 perm）
--   30 / 31 挂在 /hr（id=3）下
-- ============================================================
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(30,  3, '部门管理', 'hr:dept:view',     2, '/hr/dept',     NULL, 1),
(31,  3, '岗位管理', 'hr:position:view', 2, '/hr/position', NULL, 2);

INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(3001, 30, '新增部门', 'hr:dept:add',     3, NULL, NULL, 1),
(3002, 30, '编辑部门', 'hr:dept:edit',    3, NULL, NULL, 2),
(3003, 30, '删除部门', 'hr:dept:delete',  3, NULL, NULL, 3),
(3101, 31, '新增岗位', 'hr:position:add',     3, NULL, NULL, 1),
(3102, 31, '编辑岗位', 'hr:position:edit',    3, NULL, NULL, 2),
(3103, 31, '删除岗位', 'hr:position:delete',  3, NULL, NULL, 3);

-- ============================================================
-- 角色菜单绑定：仅给 HR 角色（id=2）追加
--   不动 ADMIN / EMPLOYEE 现有绑定
-- ============================================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 30), (2, 3001), (2, 3002), (2, 3003),
(2, 31), (2, 3101), (2, 3102), (2, 3103);

-- ============================================================
-- sys_dept 演示种子
-- ============================================================
INSERT INTO sys_dept (id, parent_id, name, code, leader, sort) VALUES
(1, 0, '总公司',     'ROOT',  '总经理',  0),
(2, 1, '研发中心',   'RND',   '张三',    1),
(3, 1, '人力资源部', 'HR',    '李四',    2),
(4, 1, '财务部',     'FIN',   '王五',    3),
(5, 2, '后端组',     'RND-B', '赵六',    1),
(6, 2, '前端组',     'RND-F', '钱七',    2);

-- ============================================================
-- 验证
-- ============================================================
SELECT 'depts' AS t, COUNT(*) FROM sys_dept
UNION ALL SELECT 'positions', COUNT(*) FROM sys_position
UNION ALL SELECT 'menus_total', COUNT(*) FROM sys_menu
UNION ALL SELECT 'role_menus_total', COUNT(*) FROM sys_role_menu;
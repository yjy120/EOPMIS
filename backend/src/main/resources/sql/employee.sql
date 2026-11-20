-- ============================================================
-- EOPMIS UC-007/008/009 职工档案 建表脚本（旧库增量升级用）
--   新增 sys_employee 表 + 追加 sys_menu / sys_role_menu
--   追加 1 条 demo 档案用于 emp01 绑定测试
--   修 sys_user.employee_id 让 emp01 看到本人档案
--
-- 执行前提：已执行过 init.sql + dept_position.sql
-- 执行顺序：在 dept_position.sql 之后执行本脚本
-- 注意：全新部署请直接使用 schema-rebuild.sql（已含本脚本内容），勿重复执行
-- ============================================================

USE eopmis;

-- ----------------------------
-- 职工档案表
--   emp_no / id_card 使用普通索引：唯一性由应用层 Service 校验
--   （count 时带 deleted=0 过滤），避免软删后同编号无法重新录入
-- ----------------------------
DROP TABLE IF EXISTS sys_employee;
CREATE TABLE sys_employee (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    emp_no          VARCHAR(32)  NOT NULL COMMENT '职工编号，应用层唯一（未删除范围）',
    name            VARCHAR(64)  NOT NULL COMMENT '姓名',
    gender          TINYINT      NOT NULL DEFAULT 1 COMMENT '1=男 2=女',
    birth_date      DATE         DEFAULT NULL COMMENT '出生日期',
    id_card         VARCHAR(32)  DEFAULT NULL COMMENT '身份证号，应用层唯一（未删除范围）',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '联系方式',
    dept_id         BIGINT       DEFAULT NULL COMMENT '所属部门ID',
    position_id     BIGINT       DEFAULT NULL COMMENT '岗位ID',
    level           VARCHAR(16)  DEFAULT NULL COMMENT '职级 P1/P2/...',
    entry_date      DATE         DEFAULT NULL COMMENT '入职日期',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0=离职 1=在职',
    user_id         BIGINT       DEFAULT NULL COMMENT '绑定的登录账号ID（普通职工）',
    remark          VARCHAR(255) DEFAULT NULL COMMENT '备注',
    deleted         TINYINT      NOT NULL DEFAULT 0,
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT       DEFAULT NULL,
    update_by       BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_emp_no (emp_no),
    KEY idx_emp_id_card (id_card),
    KEY idx_emp_dept (dept_id),
    KEY idx_emp_position (position_id),
    KEY idx_emp_status (status),
    KEY idx_emp_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职工档案';

-- ============================================================
-- 菜单与权限点（仅追加 HR 端；EMP 端复用已有菜单 21）
--   32   /hr/employee  HR 维护职工档案
--   4001/4002/4003/4004  HR 端 CRUD 按钮
--   UC-009 普通职工"我的档案"使用 init/schema 中已有的
--   菜单 21（me:profile:view, /me/profile），EMP 角色已绑定，勿重复插入
-- ============================================================
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(32,   3, '职工档案',   'hr:employee:view',    2, '/hr/employee',  NULL, 3);

-- 给 32 追加 4 个 CRUD 按钮（HR 端）
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(4001, 32, '新增职工', 'hr:employee:add',     3, NULL, NULL, 1),
(4002, 32, '编辑职工', 'hr:employee:edit',    3, NULL, NULL, 2),
(4003, 32, '删除职工', 'hr:employee:delete',  3, NULL, NULL, 3),
(4004, 32, '查看职工', 'hr:employee:detail',  3, NULL, NULL, 4);

-- ============================================================
-- 角色菜单绑定
--   HR (id=2) 追加 32 + 4001~4004
--   EMPLOYEE (id=3) 无需追加：菜单 21（me:profile:view）已在初始化脚本中绑定
-- ============================================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 32), (2, 4001), (2, 4002), (2, 4003), (2, 4004);

-- ============================================================
-- Demo 档案（让 emp01 登录后能看到本人档案）
--   注意：emp01 的 sys_user.id 以实际库为准（schema-rebuild 全新库为 3，
--         旧 init.sql 库可能为 4），执行前请先 SELECT id FROM sys_user WHERE username='emp01';
-- ============================================================
INSERT INTO sys_employee
  (id, emp_no, name, gender, birth_date, id_card, phone, dept_id, position_id, level, entry_date, status, user_id, remark)
VALUES
  (1, 'E0001', '王晓东', 1, '1995-06-15', '11010119950615001X', '13800138001', 5, NULL, 'P3', '2023-03-01', 1, 4, '演示用档案');

-- 绑定 emp01 → sys_employee.id=1（id=4 为旧库实际值，请按上一步查询结果替换）
UPDATE sys_user SET employee_id = 1 WHERE id = 4;

-- ============================================================
-- 验证
-- ============================================================
SELECT 'employees' AS t, COUNT(*) FROM sys_employee
UNION ALL SELECT 'menus_total', COUNT(*) FROM sys_menu
UNION ALL SELECT 'role_menus_total', COUNT(*) FROM sys_role_menu;
-- ============================================================
-- EOPMIS 数据库重建脚本（仅开发环境使用）
--   DROP + CREATE 所有表 + 种子数据
--   DataInitRunner 会自动校验/修正角色和账号密码
--   若生产环境执行，请先备份！
-- ============================================================

USE eopmis;

-- 业务表
DROP TABLE IF EXISTS sys_audit_log;
DROP TABLE IF EXISTS sys_password_history;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_employee;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_position;
DROP TABLE IF EXISTS sys_dept;

-- ----------------------------
-- 角色表
-- ----------------------------
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
CREATE TABLE sys_password_history (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    password     VARCHAR(128) NOT NULL COMMENT 'BCrypt 密文',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_time (user_id, create_time DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密码历史';

-- ----------------------------
-- 审计日志
-- ----------------------------
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

-- ----------------------------
-- 部门表
-- ----------------------------
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
-- 岗位表
-- ----------------------------
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

-- ----------------------------
-- 职工档案表（UC-007/008/009）
--   emp_no / id_card 唯一性由应用层 Service 校验（count + deleted=0 过滤），
--   故此处用普通索引而非 UNIQUE：否则软删后同编号档案无法重新录入
-- ----------------------------
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
-- 种子数据
-- ============================================================

-- 角色
INSERT INTO sys_role (id, code, name, description, is_system) VALUES
(1, 'ADMIN',    '系统管理员', '管账号、角色、字典、审计', 1),
(2, 'HR',       '人事专员',   '人事业务数据全权限',        1),
(3, 'EMPLOYEE', '普通职工',   '只读本人数据 + 改密',       1);

-- 菜单/权限点（UC-001~UC-004）
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(1,  0, '系统管理', NULL, 1, '/sys', 'setting', 100),
(11, 1, '账号管理', 'sys:user:view', 2, '/sys/user', NULL, 1),
(12, 1, '角色管理', 'sys:role:view', 2, '/sys/role', NULL, 2),
(13, 1, '审计日志', 'sys:audit:view', 2, '/sys/audit', NULL, 3),
(2,  0, '个人中心', NULL, 1, '/me', 'user', 50),
(21, 2, '我的资料', 'me:profile:view', 2, '/me/profile', NULL, 1),
(22, 2, '修改密码', 'me:pwd:update', 2, '/me/pwd', NULL, 2),
(3,  0, '人事业务', NULL, 1, '/hr', 'team', 10),
(1101, 11, '新增账号', 'sys:user:add', 3, NULL, NULL, 1),
(1102, 11, '启停账号', 'sys:user:enable', 3, NULL, NULL, 2),
(1103, 11, '重置密码', 'sys:user:reset', 3, NULL, NULL, 3),
(1201, 12, '编辑角色', 'sys:role:edit', 3, NULL, NULL, 1),
(1202, 12, '分配权限', 'sys:role:assign', 3, NULL, NULL, 2);

-- 部门/岗位菜单（UC-005/UC-006）
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(30,  3, '部门管理', 'hr:dept:view',     2, '/hr/dept',     NULL, 1),
(31,  3, '岗位管理', 'hr:position:view', 2, '/hr/position', NULL, 2),
(3001, 30, '新增部门', 'hr:dept:add',     3, NULL, NULL, 1),
(3002, 30, '编辑部门', 'hr:dept:edit',    3, NULL, NULL, 2),
(3003, 30, '删除部门', 'hr:dept:delete',  3, NULL, NULL, 3),
(3101, 31, '新增岗位', 'hr:position:add',     3, NULL, NULL, 1),
(3102, 31, '编辑岗位', 'hr:position:edit',    3, NULL, NULL, 2),
(3103, 31, '删除岗位', 'hr:position:delete',  3, NULL, NULL, 3);

-- 职工档案菜单（UC-007/008）
--   注意：UC-009 普通职工"我的档案"复用上方菜单 21（me:profile:view, /me/profile），不再重复建菜单
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(32,   3,  '职工档案', 'hr:employee:view',   2, '/hr/employee', NULL, 3),
(4001, 32, '新增职工', 'hr:employee:add',    3, NULL, NULL, 1),
(4002, 32, '编辑职工', 'hr:employee:edit',   3, NULL, NULL, 2),
(4003, 32, '删除职工', 'hr:employee:delete', 3, NULL, NULL, 3),
(4004, 32, '查看职工', 'hr:employee:detail', 3, NULL, NULL, 4);

-- 角色菜单绑定
-- ADMIN: 仅系统管理 + 个人中心（PRD 2.2.1：管理员不参与人事业务，
--   即使误看到人事菜单，接口层 @PreAuthorize("hasRole('HR')") 也会拦截）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 11), (1, 12), (1, 13),
(1, 1101), (1, 1102), (1, 1103), (1, 1201), (1, 1202),
(1, 2), (1, 21), (1, 22);
-- HR: 人事业务目录 + 部门/岗位/职工档案菜单与按钮
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 3), (2, 30), (2, 3001), (2, 3002), (2, 3003),
(2, 31), (2, 3101), (2, 3102), (2, 3103),
(2, 32), (2, 4001), (2, 4002), (2, 4003), (2, 4004);
-- EMPLOYEE: 个人中心
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 2), (3, 21), (3, 22);

-- 部门种子数据
INSERT INTO sys_dept (id, parent_id, name, code, leader, sort) VALUES
(1, 0, '总公司',     'ROOT',  '总经理',  0),
(2, 1, '研发中心',   'RND',   '张三',    1),
(3, 1, '人力资源部', 'HR',    '李四',    2),
(4, 1, '财务部',     'FIN',   '王五',    3),
(5, 2, '后端组',     'RND-B', '赵六',    1),
(6, 2, '前端组',     'RND-F', '钱七',    2);

-- 账号（密码用占位密文，DataInitRunner 会自动修正为正确 BCrypt）
INSERT INTO sys_user (username, real_name, password, status, role_id, fail_count, create_time, update_time) VALUES
('admin', '系统管理员', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 1, 0, NOW(), NOW()),
('hr01',  '人事专员A',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 2, 0, NOW(), NOW()),
('emp01', '普通员工1',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 1, 3, 0, NOW(), NOW());

-- Demo 职工档案（UC-009 联调）：emp01 在全新库中 sys_user.id=3，双向绑定到本档案
INSERT INTO sys_employee
  (id, emp_no, name, gender, birth_date, id_card, phone, dept_id, position_id, level, entry_date, status, user_id, remark)
VALUES
  (1, 'E0001', '王晓东', 1, '1995-06-15', '11010119950615001X', '13800138001', 5, NULL, 'P3', '2023-03-01', 1, 3, '演示用档案');

UPDATE sys_user SET employee_id = 1 WHERE id = 3;

-- ----------------------------
-- 劳动合同表（UC-010 / UC-011 / UC-012）
--   contract_no 全局唯一，唯一性由应用层 Service 校验（count + deleted=0 过滤）
--   软删除：deleted=0
-- ----------------------------
CREATE TABLE sys_contract (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    contract_no       VARCHAR(64)  NOT NULL COMMENT '合同编号，应用层唯一（未删除范围）',
    employee_id       BIGINT       NOT NULL COMMENT '职工档案ID',
    contract_type     VARCHAR(32)  DEFAULT NULL COMMENT '合同类型：固定期限/无固定期限/实习协议',
    sign_date         DATE         DEFAULT NULL COMMENT '签订日期',
    start_date        DATE         DEFAULT NULL COMMENT '开始日期',
    end_date          DATE         DEFAULT NULL COMMENT '结束日期',
    probation_months  INT          DEFAULT NULL COMMENT '试用期（月）',
    term_months       INT          DEFAULT NULL COMMENT '合同期限（月）',
    status            TINYINT      NOT NULL DEFAULT 1 COMMENT '1=有效 2=即将到期 3=已到期 4=已终止',
    terminate_date    DATE         DEFAULT NULL COMMENT '实际终止日期',
    terminate_reason  VARCHAR(255) DEFAULT NULL COMMENT '终止原因',
    remark            VARCHAR(255) DEFAULT NULL COMMENT '备注',
    deleted           TINYINT      NOT NULL DEFAULT 0,
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by         BIGINT       DEFAULT NULL,
    update_by         BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_contract_no (contract_no),
    KEY idx_contract_emp (employee_id),
    KEY idx_contract_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='劳动合同';

-- ----------------------------
-- 人事调动表（UC-013）
--   软删除：deleted=0
-- ----------------------------
CREATE TABLE sys_transfer (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id      BIGINT       NOT NULL COMMENT '职工档案ID',
    from_dept_id     BIGINT       DEFAULT NULL COMMENT '调出部门ID',
    from_position_id BIGINT       DEFAULT NULL COMMENT '原岗位ID',
    to_dept_id       BIGINT       DEFAULT NULL COMMENT '调入部门ID',
    to_position_id   BIGINT       DEFAULT NULL COMMENT '新岗位ID',
    transfer_date    DATE         DEFAULT NULL COMMENT '调动日期',
    reason           VARCHAR(255) DEFAULT NULL COMMENT '调动原因',
    status           TINYINT      NOT NULL DEFAULT 1 COMMENT '1=已生效 2=已撤销',
    deleted          TINYINT      NOT NULL DEFAULT 0,
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by        BIGINT       DEFAULT NULL,
    update_by        BIGINT       DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_transfer_emp (employee_id),
    KEY idx_transfer_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人事调动';

-- 劳动合同菜单（UC-010/011/012）
--   HR 端：/hr/contract（劳动合同管理，含新增/编辑/续签/终止/删除）
--   EMP 端：/me/contract（我的合同，只读），perm=me:contract:view
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(34,    3,  '劳动合同', 'hr:contract:view',      2, '/hr/contract', NULL, 4),
(3401, 34, '新增合同', 'hr:contract:add',       3, NULL, NULL, 1),
(3402, 34, '编辑合同', 'hr:contract:edit',     3, NULL, NULL, 2),
(3403, 34, '续签合同', 'hr:contract:renew',    3, NULL, NULL, 3),
(3404, 34, '终止合同', 'hr:contract:terminate',3, NULL, NULL, 4),
(3405, 34, '删除合同', 'hr:contract:delete',    3, NULL, NULL, 5),
(23,    2, '我的合同', 'me:contract:view',     2, '/me/contract', NULL, 3);

-- 人事调动菜单（UC-013）
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(35,    3,  '人事调动', 'hr:transfer:view',   2, '/hr/transfer', NULL, 5),
(3501, 35, '发起调动', 'hr:transfer:add',    3, NULL, NULL, 1),
(3502, 35, '编辑调动', 'hr:transfer:edit',   3, NULL, NULL, 2),
(3503, 35, '撤销调动', 'hr:transfer:revoke', 3, NULL, NULL, 3),
(3504, 35, '删除调动', 'hr:transfer:delete', 3, NULL, NULL, 4);

-- 角色菜单绑定（UC-010/011/012/013 新增部分）
-- HR: 劳动合同管理 + 人事调动 全部按钮
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 34), (2, 3401), (2, 3402), (2, 3403), (2, 3404), (2, 3405),
(2, 35), (2, 3501), (2, 3502), (2, 3503), (2, 3504);
-- EMPLOYEE: 我的合同（UC-012 普通职工查看本人合同）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 23);

-- Demo 合同数据（UC-009/UC-012 联调）：王晓东（id=1）的有效合同
INSERT INTO sys_contract
  (id, contract_no, employee_id, contract_type, sign_date, start_date, end_date,
   probation_months, term_months, status, remark, create_by, update_by)
VALUES
  (1, 'C20230301001', 1, '固定期限', '2023-03-01', '2023-03-01', '2026-02-28',
   6, 36, 1, '演示用合同', 1, 1);

-- ----------------------------
-- 薪资记录表（UC-014 / UC-016）
--   (employee_id, salary_month) 唯一性由应用层 Service 校验（countByEmployeeMonth + deleted=0 过滤）
--   salary_month 格式 YYYY-MM
--   软删除：deleted=0
-- ----------------------------
CREATE TABLE sys_salary (
    id                 BIGINT        NOT NULL AUTO_INCREMENT,
    employee_id        BIGINT        NOT NULL COMMENT '职工档案ID',
    salary_month       VARCHAR(7)    NOT NULL COMMENT '薪资月份 YYYY-MM',
    base_salary        DECIMAL(12,2) DEFAULT 0.00 COMMENT '基本工资',
    position_salary    DECIMAL(12,2) DEFAULT 0.00 COMMENT '岗位工资',
    performance_salary DECIMAL(12,2) DEFAULT 0.00 COMMENT '绩效工资',
    allowance          DECIMAL(12,2) DEFAULT 0.00 COMMENT '补贴',
    deduction          DECIMAL(12,2) DEFAULT 0.00 COMMENT '扣款',
    actual_salary      DECIMAL(12,2) DEFAULT 0.00 COMMENT '实发工资',
    payment_date       DATE          DEFAULT NULL COMMENT '发放日期',
    remark             VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    deleted            TINYINT       NOT NULL DEFAULT 0,
    create_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by          BIGINT        DEFAULT NULL,
    update_by          BIGINT        DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_salary_emp (employee_id),
    KEY idx_salary_month (salary_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪资记录';

-- ----------------------------
-- 考勤记录表（UC-015 / UC-017）
--   status: 1=出勤 2=迟到 3=早退 4=请假 5=加班 6=缺勤
--   (employee_id, attendance_date) 唯一性由应用层 Service 校验
--   软删除：deleted=0
-- ----------------------------
CREATE TABLE sys_attendance (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    employee_id      BIGINT        NOT NULL COMMENT '职工档案ID',
    attendance_date  DATE          NOT NULL COMMENT '考勤日期',
    status           TINYINT       NOT NULL COMMENT '1=出勤 2=迟到 3=早退 4=请假 5=加班 6=缺勤',
    leave_type       VARCHAR(32)   DEFAULT NULL COMMENT '请假类型（status=4）',
    overtime_hours   DECIMAL(6,2)  DEFAULT NULL COMMENT '加班时长（小时，status=5）',
    remark           VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    deleted          TINYINT       NOT NULL DEFAULT 0,
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by        BIGINT        DEFAULT NULL,
    update_by        BIGINT        DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_att_emp (employee_id),
    KEY idx_att_date (attendance_date),
    KEY idx_att_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录';

-- 薪资管理菜单（UC-014）
--   HR 端：/hr/salary（薪资管理，含新增/编辑/删除）
--   EMP 端：/me/salary（我的薪资，只读），perm=me:salary:view
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(36,    3,  '薪资管理', 'hr:salary:view',      2, '/hr/salary', NULL, 6),
(3601, 36, '新增薪资', 'hr:salary:add',       3, NULL, NULL, 1),
(3602, 36, '编辑薪资', 'hr:salary:edit',      3, NULL, NULL, 2),
(3603, 36, '删除薪资', 'hr:salary:delete',    3, NULL, NULL, 3),
(24,    2, '我的薪资', 'me:salary:view',     2, '/me/salary', NULL, 4);

-- 考勤管理菜单（UC-015）
--   HR 端：/hr/attendance（考勤管理，含新增/编辑/删除）
--   EMP 端：/me/attendance（我的考勤，只读），perm=me:attendance:view
INSERT INTO sys_menu (id, parent_id, name, perm, type, path, icon, sort) VALUES
(37,    3,  '考勤管理', 'hr:attendance:view',   2, '/hr/attendance', NULL, 7),
(3701, 37, '新增考勤', 'hr:attendance:add',    3, NULL, NULL, 1),
(3702, 37, '编辑考勤', 'hr:attendance:edit',   3, NULL, NULL, 2),
(3703, 37, '删除考勤', 'hr:attendance:delete', 3, NULL, NULL, 3),
(25,    2, '我的考勤', 'me:attendance:view',  2, '/me/attendance', NULL, 5);

-- 角色菜单绑定（UC-014/015/016/017 新增部分）
-- HR: 薪资管理 + 考勤管理 全部按钮
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 36), (2, 3601), (2, 3602), (2, 3603),
(2, 37), (2, 3701), (2, 3702), (2, 3703);
-- EMPLOYEE: 我的薪资 + 我的考勤
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 24), (3, 25);

-- HR: 个人中心（修改密码 + 本人档案/合同/薪资/考勤）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 2), (2, 21), (2, 22), (2, 23), (2, 24), (2, 25);

-- Demo 薪资数据（UC-016 联调）：王晓东（employee_id=1）2026-08 薪资
INSERT INTO sys_salary
  (id, employee_id, salary_month, base_salary, position_salary, performance_salary,
   allowance, deduction, actual_salary, payment_date, remark, create_by, update_by)
VALUES
  (1, 1, '2026-08', 8000.00, 3000.00, 2000.00, 500.00, 300.00, 13200.00, '2026-09-05', '演示用薪资', 1, 1);

-- Demo 考勤数据（UC-017 联调）：王晓东（employee_id=1）近几天考勤
INSERT INTO sys_attendance
  (id, employee_id, attendance_date, status, remark, create_by, update_by)
VALUES
  (1, 1, '2026-09-15', 1, '演示考勤-出勤', 1, 1),
  (2, 1, '2026-09-16', 1, '演示考勤-出勤', 1, 1),
  (3, 1, '2026-09-17', 1, '演示考勤-出勤', 1, 1);

-- Rename menus to Chinese (only menus; users/roles can keep English code)
UPDATE sys_menu SET name = '系统管理' WHERE id = 1;
UPDATE sys_menu SET name = '账号管理' WHERE id = 11;
UPDATE sys_menu SET name = '角色管理' WHERE id = 12;
UPDATE sys_menu SET name = '审计日志' WHERE id = 13;
UPDATE sys_menu SET name = '个人中心' WHERE id = 2;
UPDATE sys_menu SET name = '我的资料' WHERE id = 21;
UPDATE sys_menu SET name = '修改密码' WHERE id = 22;
UPDATE sys_menu SET name = '人事业务' WHERE id = 3;
UPDATE sys_menu SET name = '新增账号' WHERE id = 1101;
UPDATE sys_menu SET name = '启停账号' WHERE id = 1102;
UPDATE sys_menu SET name = '重置密码' WHERE id = 1103;
UPDATE sys_menu SET name = '编辑角色' WHERE id = 1201;
UPDATE sys_menu SET name = '分配权限' WHERE id = 1202;

UPDATE sys_role SET name = '系统管理员', description = '管账号、角色、字典、审计' WHERE code = 'ADMIN';
UPDATE sys_role SET name = '人事专员',   description = '人事业务数据全权限'        WHERE code = 'HR';
UPDATE sys_role SET name = '普通职工',   description = '只读本人数据 + 改密'       WHERE code = 'EMPLOYEE';

UPDATE sys_user SET real_name = '系统管理员' WHERE username = 'admin';
UPDATE sys_user SET real_name = '人事专员A'  WHERE username = 'hr01';
UPDATE sys_user SET real_name = '普通员工1'  WHERE username = 'emp01';

SELECT id, code, name FROM sys_role;
SELECT id, parent_id, name, perm FROM sys_menu ORDER BY parent_id, sort;
SELECT id, username, real_name, status, role_id FROM sys_user;

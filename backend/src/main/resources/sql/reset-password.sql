-- 启动后用一段 SQL 把 admin/hr01/emp01 的密码重置为已知明文
-- 执行：先用任意工具调用 PasswordUtil.hash() 得到密文，再 UPDATE
-- 或者直接用项目中的 PasswordEncoder（见后端 PasswordUtil）
--
-- 简化方案：在数据库中删除账号后用 Java 端 BCrypt 写回
-- 这里给出推荐做法（首启时由后端用命令行 runner 注入，下面只用于手工兜底）

-- 步骤 1：临时关掉密码策略
UPDATE sys_user SET password = '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5Y5Y5/4Xn4K3qH8W8hZx9Y9Y9Y9Y9'
WHERE username IN ('admin','hr01','emp01');

-- 步骤 2：登录后调 /auth/changePassword 改成正式密码
-- 步骤 3：或者调用 PasswordUtil 的单元测试一次性输出新 BCrypt 替换上面密文
--
-- ⚠️ 上述密文仅为占位（结构合法但与明文不匹配）。
-- 第一次部署时务必按 README 中"重置 admin 密码"小节生成真实密文。

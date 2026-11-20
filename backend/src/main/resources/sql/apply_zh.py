"""Apply Chinese names to EOPMIS seed data via direct MySQL connection."""
import pymysql

conn = pymysql.connect(
    host='127.0.0.1', port=3306, user='root', password='YOUR_DB_PASSWORD',
    database='eopmis', charset='utf8mb4'
)
cur = conn.cursor()

updates = [
    ("UPDATE sys_menu SET name='系统管理' WHERE id=1", None),
    ("UPDATE sys_menu SET name='账号管理' WHERE id=11", None),
    ("UPDATE sys_menu SET name='角色管理' WHERE id=12", None),
    ("UPDATE sys_menu SET name='审计日志' WHERE id=13", None),
    ("UPDATE sys_menu SET name='个人中心' WHERE id=2", None),
    ("UPDATE sys_menu SET name='我的资料' WHERE id=21", None),
    ("UPDATE sys_menu SET name='修改密码' WHERE id=22", None),
    ("UPDATE sys_menu SET name='人事业务' WHERE id=3", None),
    ("UPDATE sys_menu SET name='新增账号' WHERE id=1101", None),
    ("UPDATE sys_menu SET name='启停账号' WHERE id=1102", None),
    ("UPDATE sys_menu SET name='重置密码' WHERE id=1103", None),
    ("UPDATE sys_menu SET name='编辑角色' WHERE id=1201", None),
    ("UPDATE sys_menu SET name='分配权限' WHERE id=1202", None),
    ("UPDATE sys_role SET name='系统管理员', description='管账号、角色、字典、审计' WHERE code='ADMIN'", None),
    ("UPDATE sys_role SET name='人事专员',   description='人事业务数据全权限'        WHERE code='HR'", None),
    ("UPDATE sys_role SET name='普通职工',   description='只读本人数据 + 改密'       WHERE code='EMPLOYEE'", None),
    ("UPDATE sys_user SET real_name='系统管理员' WHERE username='admin'", None),
    ("UPDATE sys_user SET real_name='人事专员A'  WHERE username='hr01'", None),
    ("UPDATE sys_user SET real_name='普通员工1'  WHERE username='emp01'", None),
]

for sql, _ in updates:
    cur.execute(sql)

conn.commit()

# Verify
cur.execute("SELECT id, code, name, description FROM sys_role ORDER BY id")
print("--- sys_role ---")
for r in cur.fetchall(): print(r)

cur.execute("SELECT id, parent_id, name FROM sys_menu ORDER BY parent_id, sort")
print("--- sys_menu ---")
for r in cur.fetchall(): print(r)

cur.execute("SELECT id, username, real_name, status, role_id FROM sys_user")
print("--- sys_user ---")
for r in cur.fetchall(): print(r)

cur.close()
conn.close()
print("DONE")

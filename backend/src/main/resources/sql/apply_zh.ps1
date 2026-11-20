$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$args  = @('--default-character-set=utf8mb4', '-h127.0.0.1', '-P3306', '-uroot', '-pYOUR_DB_PASSWORD', 'eopmis')

# Pair: (where clause column, where value, new Chinese value)
$updates = @(
    "UPDATE sys_menu SET name='系统管理' WHERE id=1",
    "UPDATE sys_menu SET name='账号管理' WHERE id=11",
    "UPDATE sys_menu SET name='角色管理' WHERE id=12",
    "UPDATE sys_menu SET name='审计日志' WHERE id=13",
    "UPDATE sys_menu SET name='个人中心' WHERE id=2",
    "UPDATE sys_menu SET name='我的资料' WHERE id=21",
    "UPDATE sys_menu SET name='修改密码' WHERE id=22",
    "UPDATE sys_menu SET name='人事业务' WHERE id=3",
    "UPDATE sys_menu SET name='新增账号' WHERE id=1101",
    "UPDATE sys_menu SET name='启停账号' WHERE id=1102",
    "UPDATE sys_menu SET name='重置密码' WHERE id=1103",
    "UPDATE sys_menu SET name='编辑角色' WHERE id=1201",
    "UPDATE sys_menu SET name='分配权限' WHERE id=1202",
    "UPDATE sys_role SET name='系统管理员', description='管账号、角色、字典、审计' WHERE code='ADMIN'",
    "UPDATE sys_role SET name='人事专员',   description='人事业务数据全权限'        WHERE code='HR'",
    "UPDATE sys_role SET name='普通职工',   description='只读本人数据 + 改密'       WHERE code='EMPLOYEE'",
    "UPDATE sys_user SET real_name='系统管理员' WHERE username='admin'",
    "UPDATE sys_user SET real_name='人事专员A'  WHERE username='hr01'",
    "UPDATE sys_user SET real_name='普通员工1'  WHERE username='emp01'"
)

# Write as a temp UTF-8 BOM file, then `source` it via a single mysql call.
$tmp = 'c:\Users\Lenovo\Desktop\EOPMIS\backend\src\main\resources\sql\zh_updates.sql'
$utf8Bom = New-Object System.Text.UTF8Encoding $true
$sb = New-Object System.Text.StringBuilder
foreach ($u in $updates) { [void]$sb.AppendLine($u) }
[System.IO.File]::WriteAllText($tmp, $sb.ToString(), $utf8Bom)
Write-Host "Wrote: $tmp"

# Use cmd redirection to feed file content to mysql stdin.
$proc = Start-Process -FilePath $mysql -ArgumentList $args -RedirectStandardInput $tmp -NoNewWindow -Wait -PassThru
Write-Host "Exit: $($proc.ExitCode)"

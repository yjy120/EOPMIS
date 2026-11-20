$src = 'c:\Users\Lenovo\Desktop\EOPMIS\backend\src\main\resources\sql\seed_zh.sql'
$dst = 'c:\Users\Lenovo\Desktop\EOPMIS\backend\src\main\resources\sql\seed_zh_bom.sql'
$content = Get-Content $src -Raw
$utf8Bom = New-Object System.Text.UTF8Encoding $true
[System.IO.File]::WriteAllText($dst, $content, $utf8Bom)
Write-Host "Wrote BOM file: $dst"
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' --default-character-set=utf8mb4 -h 127.0.0.1 -P 3306 -u root -p123456 eopmis -e "source c:/Users/Lenovo/Desktop/EOPMIS/backend/src/main/resources/sql/seed_zh_bom.sql"

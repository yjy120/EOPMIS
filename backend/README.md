# EOPMIS 第 1 周实现：4 个用例

本目录代码仅实现 PRD 中给出的 4 个用例，其余业务模块在后续迭代中按 M01~M10 排期开发。

| 用例   | 标题     | 后端 Controller            | 前端 View           |
|--------|----------|----------------------------|---------------------|
| UC-001 | 用户登录 | `AuthController#login`     | `Login.vue`         |
| UC-002 | 修改密码 | `AuthController#changePassword` | `ChangePassword.vue` |
| UC-003 | 账号管理 | `UserAdminController`      | `AccountManage.vue` |
| UC-004 | 角色权限 | `RoleController`           | `RoleManage.vue`    |

---

## 一、目录结构

```
backend/
├── pom.xml
└── src/main/
    ├── java/com/eopmis/
    │   ├── EopmisApplication.java         # 启动类
    │   ├── common/                        # Result / BizException / GlobalExceptionHandler
    │   ├── config/                        # MybatisPlus / Cors / DataInitRunner
    │   ├── controller/                    # AuthController / UserAdminController / RoleController
    │   ├── dto/                           # 请求/响应 DTO
    │   ├── entity/                        # 6 张 sys_* 实体
    │   ├── mapper/                        # MyBatis-Plus Mapper
    │   ├── security/                      # SecurityConfig / JwtAuthFilter / LoginUser / CurrentUser
    │   ├── service/ + service/impl/       # 业务实现
    │   └── util/                          # JwtUtil / PasswordUtil
    └── resources/
        ├── application.yml                # 数据源 / JWT / 密码策略
        ├── mapper/                        # SysMenuMapper.xml / SysPasswordHistoryMapper.xml
        └── sql/init.sql                   # 建表 + 种子数据

frontend/
├── package.json
├── vite.config.js
├── index.html
└── src/
    ├── main.js
    ├── App.vue
    ├── style.css
    ├── api/                              # auth / user / role
    ├── router/                           # 路由 + 守卫
    ├── store/                            # Pinia user store
    ├── utils/                            # request.js（Axios 封装）
    └── views/                            # Login / Layout / Dashboard / ChangePassword / AccountManage / RoleManage
```

---

## 二、启动步骤

### 1. 数据库
```bash
mysql -u root -p < backend/src/main/resources/sql/init.sql
```

### 2. 启动后端
```bash
cd backend
mvn spring-boot:run
# 第一次启动会自动：建角色、补账号、重置默认密码
```

后端默认监听 `http://localhost:8080`，接口前缀 `/api`，Swagger UI: `http://localhost:8080/api/doc.html`

### 3. 启动前端
```bash
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`

---

## 三、默认账号（启动时由 DataInitRunner 自动校验/重置）

| 账号  | 密码          | 角色       | 用途            |
|-------|---------------|------------|-----------------|
| admin | `YOUR_ADMIN_PWD`   | 系统管理员 | 账号/角色管理   |
| hr01  | `Hr@YOUR_DB_PASSWORD`   | 人事专员   | 人事业务（本初版未开放页面） |
| emp01 | `Emp@YOUR_DB_PASSWORD`  | 普通职工   | 仅改密          |

> 注：若 `init.sql` 中 BCrypt 密文是占位（首次执行后登录失败），`DataInitRunner` 会在后端启动时把 admin 密码刷成 `YOUR_ADMIN_PWD`。这意味着只要后端能起来，登录就能成功。

---

## 四、接口清单

### 4.1 认证

| 方法 | 路径                       | 鉴权       | 说明                       |
|------|----------------------------|------------|----------------------------|
| POST | `/api/auth/login`          | 无         | UC-001 登录                |
| POST | `/api/auth/logout`         | 已登录     | 登出（审计）               |
| POST | `/api/auth/changePassword` | 已登录     | UC-002 改密                |

### 4.2 账号管理（仅 ADMIN）

| 方法 | 路径                                  | 说明                  |
|------|---------------------------------------|-----------------------|
| GET  | `/api/admin/user/page`                | 分页查询              |
| POST | `/api/admin/user`                     | UC-003 新增账号       |
| POST | `/api/admin/user/{id}/enable`         | 启用                  |
| POST | `/api/admin/user/{id}/disable`        | 停用                  |
| POST | `/api/admin/user/{id}/resetPassword`  | 重置密码              |

### 4.3 角色与权限（仅 ADMIN）

| 方法 | 路径                          | 说明                       |
|------|-------------------------------|----------------------------|
| GET  | `/api/admin/role`             | 角色列表（含 menuIds）     |
| GET  | `/api/admin/role/{id}`        | 角色详情                   |
| GET  | `/api/admin/role/menu/all`    | 全部菜单                   |
| POST | `/api/admin/role/assign`      | UC-004 分配菜单            |

### 4.4 公共返回体

```json
{ "code": 0, "message": "ok", "data": ... }
```

错误码：
- `40001` 账号或密码错误 / `40002` 账号已锁定 / `40003` 账号已停用
- `40011` 旧密码错误 / `40012` 两次输入不一致 / `40013` 密码与历史重复 / `40014` 不符合密码策略
- `40021` 账号名已存在 / `40022` 角色不存在 / `40023` 不能停用当前账号 / `40024` 存在业务数据
- `40031` 至少一个菜单 / `40032` 角色被引用
- `401` 未登录 / `403` 无权限

---

## 五、用例 → 代码对照

### UC-001 用户登录
- 入口：`AuthController#login` → `AuthServiceImpl#login`
- 关键实现：
  - **4a 账号不存在 / 4b 密码错误**：统一返回 `账号或密码错误` 防枚举；`fail_count` 自增，达 5 次后 `locked_until=now+15min` 并重置计数
  - **5a 账号停用**：返回 `账号已停用`
  - **6a 系统异常**：由 `GlobalExceptionHandler` 兜底返回 500
  - 主成功场景：签发 JWT（30 分钟），返回菜单树 + 权限点 + 角色信息
- 审计：`sys_audit_log` 写 LOGIN 成功 / LOGIN_FAIL（含失败原因）
- 单测建议：连续 5 次错密码 → 第 6 次正确密码应被拒

### UC-002 修改个人密码
- 入口：`AuthController#changePassword` → `AuthServiceImpl#changePassword`
- 关键实现（`@Transactional`）：
  - **4a 旧密码错误**：BCrypt 校验失败抛 `40011`
  - **5a 密码策略**：`pwd.length>=8 && 包含字母 && 包含数字`
  - **6a 历史重复**：`sys_password_history` 取最近 3 条 BCrypt 密文逐一比对
  - **7a 两次输入不一致**：DTO 自定义校验
  - **7 写新密码**：`sys_user.password` 更新 + 写一条密码历史
  - **8 强制退出**：前端清 token 跳登录页（不删会话，后端无状态）
- 前端：`ChangePassword.vue` 校验通过后调用 `logoutAction` 并跳登录

### UC-003 账号管理
- 入口：`UserAdminController`（`@PreAuthorize("hasRole('ADMIN')")`）
- 关键实现：
  - **3a 账号名重复**：`SysUserMapper#selectByUsername` 预检
  - **3b 必填项**：`@Valid` + `@NotBlank/@NotNull`
  - **4a 不能停用当前账号**：`id.equals(currentUserId)` 时抛 `40023`
  - **5a 关联业务数据**：`employeeId != null` 时抛 `40024`
  - 启用/停用/重置：分别更新 `status` / 写回默认 BCrypt 密码

### UC-004 角色权限分配
- 入口：`RoleController#assign`（仅 ADMIN）→ `RoleServiceImpl#saveMenus`
- 关键实现（`@Transactional`）：
  - **2a 至少一个菜单**：`menuIds.isEmpty()` 抛 `40031`
  - **3a 角色被引用**：`SysUserMapper#selectCount(role_id=X, status=1)` > 0 时抛 `40032`
  - 先 `deleteByRoleId` 再 `insertBatch` 角色-菜单关联
  - 角色列表/详情/全量菜单/分配 4 个接口全部就绪
- 前端：`RoleManage.vue` 用 Element Plus `el-tree` + 复选框，半选父节点也会一并提交

---

## 六、与其他模块的关系

- 本次仅实现 M01（账号/登录）和 M10（角色/权限）框架，不实现 M02~M09 业务页面
- 业务表按 `docs/数据库设计文档.md` 后续补齐即可，账号-员工绑定（`employee_id`）的逻辑在引入员工表时无须改动后端 UserAdminService
- 后续接入新菜单时，只需在 `sys_menu` 加记录 + 在 `sys_role_menu` 关联到 ADMIN/HR/EMPLOYEE 三角色即可

---

## 七、运行验证

1. 启动后端 → 启动前端 → 浏览器打开 `http://localhost:5173`
2. 用 `admin / YOUR_ADMIN_PWD` 登录
3. 进入"账号管理"→ 新增一个测试账号 → 列表中可见
4. 进入"角色管理"→ 选中"系统管理员"→ 调整勾选 → 保存
5. 进入"修改密码"→ 改密成功后自动跳登录页
6. 用错密码连试 5 次，第 6 次会被锁定 15 分钟

# WBS 任务拆解清单：开发阶段排期

> 项目：企业职工人事在线管理信息系统（EOPMIS）
> 编制日期：2026-09-10
> 依据文档：《PRD》V1.0、《概要设计说明书》V0.2、《详细设计说明书》V0.2、《数据库设计文档》V0.2、《测试方案与用例集》V0.1、PROJECT.md
> 技术栈：前端 Vue3 + Element Plus（Vite + Vue Router + Pinia + Axios）；后端 Spring Boot 2.7 + MyBatis-Plus + Spring Security + JWT；数据库 MySQL 8.0
> 任务粒度约定：**每条任务 = 一条独立提示词，单次提交 AI 即可完成并自测**；任务按依赖先后排序，前置任务未完成不得执行后续任务。

---

## 本周范围（公共模块）与边界

**本周开发（W01~W18）**：

| 范围 | 对应需求 |
|------|----------|
| 用户登录 / 登出 / 会话 | UC-001、FR-003 |
| 修改个人密码 | UC-002、FR-004 |
| 系统用户账号管理（增查改、停用启用、重置密码） | UC-003、FR-001 |
| 角色与权限分配（角色 CRUD、菜单树、角色授权） | UC-004、FR-002 |
| 操作审计日志记录与查询 | FR-022 |
| 前后端底层公共工程底座（脚手架、统一返回体、异常处理、Security/JWT、AOP、前端框架） | 全部模块的公共基础 |

**本周明确不做（后续 WBS 排期）**：部门 M03、岗位 M04、员工档案 M02、劳动合同 M05、人事调动 M06、薪资 M07、考勤 M08、请假审批 M09 的全部业务功能与页面。

**特别说明**：

1. 本系统**无用户自助注册功能**，账号仅由系统管理员后台创建，所有任务不得生成注册接口/注册页面。
2. `sys_user.employee_id`（账号-员工绑定）字段保留、可空；本周不实现绑定选择器（员工数据尚不存在），创建 EMPLOYEE 角色账号时该字段暂留空，绑定功能随 M02 员工档案模块上线。
3. 本周接口范围为详细设计说明书 API-001 ~ API-019；API-020 起为人事业务接口，本周不实现。
4. 后端包结构遵循 PROJECT.md：`com.eopmis` 下 `config/controller/service(+impl)/mapper/entity/dto/vo/exception/util`；前端目录 `src` 下 `api/views/components/router/store/utils`。
5. 后端关键业务逻辑（登录锁定、密码历史、账号停用校验等）须随代码提交 JUnit 5 单元测试。

---

## 阶段一：前后端工程底座（W01~W03）

### W01 后端 Spring Boot 工程脚手架搭建

**前置依赖**：无
**任务简述**：创建 backend 后端工程，完成依赖、配置、分层包结构、统一返回体、全局异常处理、MyBatis-Plus/Druid 配置，应用可启动。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：在仓库根目录创建 `backend/` Spring Boot 后端工程脚手架（本周只做公共底座，不实现任何人事实业务接口）。
> 要求：
> 1. Maven 工程，JDK 11，Spring Boot 2.7.x，包名 `com.eopmis`；引入依赖：spring-boot-starter-web、spring-boot-starter-security、spring-boot-starter-validation、mybatis-plus-boot-starter（3.5.x）、mysql-connector-j、druid-spring-boot-starter（1.2.x）、jjwt（0.11.x）、lombok、spring-boot-starter-test。
> 2. 按 PROJECT.md 建立包结构：`config/controller/service/impl/mapper/entity/dto/vo/exception/util`，并在 `resources/` 下建立 `mapper/`、`application.yml`（多环境 profile：dev/prod）。
> 3. `application-dev.yml` 配置：MySQL 数据源 `jdbc:mysql://localhost:3306/eopmis`、Druid 连接池、MyBatis-Plus（id-type=auto、map-underscore-to-camel-case、mapper-xml 路径）、服务端口 8080；对外接口路径以 `/api` 开头（与详细设计说明书接口路径完全一致）。
> 4. 编写统一返回体 `Result<T>`（code/message/data）、业务状态码枚举 `ResultCode`（200/400/401/403/500 等）、业务异常 `BusinessException`、全局异常处理器 `GlobalExceptionHandler`（@RestControllerAdvice，统一处理参数校验异常、业务异常、权限异常、系统异常，返回标准 Result）。
> 5. 编写 MyBatis-Plus 分页插件配置类与 Druid 监控配置；编写 CORS 配置。
> 6. 提供一个冒烟接口 `GET /api/ping` 返回成功，用于验证工程可启动。
> 7. 本任务不要写任何业务 Controller/Service/Mapper；Security 先放开全部路径（后续任务接管）。
> 验收：`mvn spring-boot:run` 启动无报错，访问 `/api/ping` 返回 `{"code":200,...}`。

---

### W02 数据库建库建表与初始化数据落地（含本周菜单权限种子数据）

**前置依赖**：W01
**任务简述**：将数据库设计文档的建表 SQL 与初始化数据落为工程内可执行脚本并验证；补充本周系统管理所需的菜单权限种子数据与管理员授权数据。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：完成 eopmis 数据库的建库建表、初始化数据落地与验证（本周只初始化公共模块所需数据）。
> 要求：
> 1. 在本地 MySQL 8.0 中创建数据库 `eopmis`（utf8mb4 / utf8mb4_unicode_ci / InnoDB）。
> 2. 将《数据库设计文档》§四的 21 张表建表 SQL、§五的初始化数据（三类内置角色 ADMIN/HR/EMPLOYEE、admin 初始账号 admin/YOUR_ADMIN_PWD 的 BCrypt 哈希、ATTENDANCE_STATUS/LEAVE_TYPE/CONTRACT_TYPE/FAMILY_RELATION 字典、顶级部门"公司总部"）整理为工程脚本文件：`backend/src/main/resources/sql/schema.sql`（建表）与 `data.sql`（初始化数据），并实际执行成功。
> 3. **补充本周菜单权限种子数据**（《数据库设计文档》§五未包含，需新增插入 `sys_permission` 与 `sys_role_permission`）：
>    - 目录"系统管理"（type=1，path=/system，icon=setting，sort=10）；
>    - 其下菜单：账号管理（type=2，path=/system/account，code=`system:account`）、角色管理（path=/system/role，code=`system:role`）、操作日志（path=/system/audit，code=`system:audit`）；
>    - 按钮/接口权限点（type=3，父菜单对应）：`account:add`、`account:edit`、`account:status`、`account:reset`、`role:add`、`role:edit`、`role:delete`、`role:assign`；
>    - 个人密码权限点 `auth:password`（type=3，挂根目录）；
>    - 首页菜单"首页"（type=2，path=/home，code=`home`，sort=1）；
>    - 将以上全部权限授予 ADMIN 角色（role_id=1），写入 `sys_role_permission`；EMPLOYEE/HR 角色本周仅授 `home` 与 `auth:password`（业务菜单待后续模块补充）。
> 4. 种子数据同时追加进 `data.sql`，保证新环境一次执行可复现。
> 5. 验证：表数量 21 张；`sys_user` 中 admin 账号存在；`sys_permission` 本周菜单/按钮数据齐全；ADMIN 角色关联权限条数正确。
> 注意：不得修改《数据库设计文档》已定的表结构与字段；只新增种子数据。

---

### W03 前端 Vue3 工程脚手架搭建

**前置依赖**：无（可与 W01/W02 并行，但联调前必须完成）
**任务简述**：创建 frontend 前端工程，完成 Vite/Vue3/Element Plus/Router/Pinia/Axios 基础骨架与请求封装。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：在仓库根目录创建 `frontend/` Vue3 前端工程脚手架（本周只搭底座与登录相关，不实现人事业务页面）。
> 要求：
> 1. Vite + Vue 3（组合式 API，`<script setup>`）+ Element Plus + Vue Router 4 + Pinia + Axios；按 PROJECT.md 建立 `src/api`、`src/views`、`src/components`、`src/router`、`src/store`、`src/utils` 目录。
> 2. 配置 Vite 开发代理：`/api` 代理到 `http://localhost:8080`，开发端口 5173。
> 3. 封装 Axios 实例（`src/utils/request.js`）：baseURL=`/api`；请求拦截器从 localStorage 取 token 并加 Header `Authorization: Bearer <token>`；响应拦截器统一处理后端 `Result` 结构——code=200 返回 data，401 清除登录态并跳转登录页，403 提示"无权限访问"，其他业务错误用 ElMessage 提示 message；超时与网络错误有友好提示。
> 4. 建立 Pinia `user` store 骨架（state：token、userInfo、menus；actions：login、logout、getUserInfo 先留空实现/占位）。
> 5. 建立路由骨架：`/login` 登录页（占位空白页即可，登录页在后续任务实现）、`/` 重定向规则；路由守卫先实现"无 token 跳 /login"的基础逻辑。
> 6. 建立 `src/api` 模块文件占位（`auth.js`、`system/account.js`、`system/role.js`、`system/audit.js`），按详细设计接口路径封装函数骨架。
> 7. 本任务不实现注册页、不实现任何人事业务页面。
> 验收：`npm run dev` 启动无报错；访问根路径未登录时跳转 /login；请求 `/api/ping` 经代理可通。

---

## 阶段二：后端公共组件（W04~W06）

### W04 系统底座 7 张表实体类与 Mapper 层

**前置依赖**：W01、W02
**任务简述**：完成 sys_user/sys_role/sys_permission/sys_role_permission/sys_password_history/sys_audit_log/sys_dict 七张表的 Entity、Mapper、公共字段自动填充。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：在 `backend/` 中完成系统底座 7 张表的实体类与数据访问层（本周只做底座表，不创建 dept/position/employee 等业务表的实体）。
> 要求：
> 1. 按《数据库设计文档》§3.1~§3.7 的字段、类型、注释，编写实体类：`SysUser`、`SysRole`、`SysPermission`、`SysRolePermission`、`SysPasswordHistory`、`SysAuditLog`、`SysDict`（放 `entity` 包），使用 MyBatis-Plus 注解（@TableName、@TableId、@TableField），字段名严格对应蛇形列名。
> 2. 编写公共基类 `BaseEntity`（createTime、updateTime）与需要时的 createBy/updateBy 支持；编写 `MyMetaObjectHandler` 实现 create_time/update_time 自动填充。
> 3. 编写对应 Mapper 接口（继承 BaseMapper，放 `mapper` 包）：SysUserMapper、SysRoleMapper、SysPermissionMapper、SysRolePermissionMapper、SysPasswordHistoryMapper、SysAuditLogMapper、SysDictMapper；启动类加 @MapperScan。
> 4. 在 Mapper 中用注解或 XML 实现本周需要的自定义查询：按 username 查用户（含角色信息）、按 roleId 查权限 ID 列表与权限列表、按 userId 查最近 3 条密码历史（按 create_time 倒序）。
> 5. 编写枚举：账号状态（1启用/0停用）、权限类型（1目录/2菜单/3按钮）。
> 6. 编写 JUnit 5 测试（@SpringBootTest）验证：按 username 能查到 admin 用户及其角色；按 roleId=1 能查到 W02 种子的权限列表。
> 验收：测试通过；实体字段与数据库列一一对应，无遗漏字段。

---

### W05 Spring Security + JWT 认证授权底座

**前置依赖**：W04
**任务简述**：完成 JWT 工具、JWT 认证过滤器、Security 过滤链配置、401/403 处理、当前登录用户上下文。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：在 `backend/` 中实现 Spring Security + JWT 认证授权底座（不含登录业务逻辑，登录接口在后续任务实现）。
> 要求：
> 1. `JwtUtils`：使用 jjwt 生成与解析 JWT，载荷含 userId、username、roleCode；签名密钥写在 application.yml（不硬编码）；令牌有效期 30 分钟；提供生成、解析、校验过期方法。
> 2. `JwtAuthenticationFilter`：继承 OncePerRequestFilter，从 Header `Authorization: Bearer <token>` 解析令牌，校验通过后构建 UsernamePasswordAuthenticationToken（权限为角色标识 `ROLE_ADMIN`/`ROLE_HR`/`ROLE_EMPLOYEE`）写入 SecurityContext；解析失败/过期不抛 500，交由后续入口点返回 401。
> 3. `SecurityConfig`：无状态会话（STATELESS）；放行 `POST /api/auth/login`；其余 `/api/**` 全部需认证；注册 JWT 过滤器；提供 BCryptPasswordEncoder Bean。
> 4. `AuthenticationEntryPoint` 实现：未认证访问返回 HTTP 401 + Result JSON（code=401，message="未登录或登录已过期"）；`AccessDeniedHandler` 实现：权限不足返回 403 + Result JSON（message="无权限访问"）。
> 5. `SecurityUtils`（或 LoginUserContext）：提供获取当前登录用户 userId、username、roleCode 的静态方法，从 SecurityContext 读取。
> 6. 临时提供一个受保护测试接口 `GET /api/auth/test-protected` 验证：无 token 返回 401；携带手工生成的有效 token 返回 200；角色不匹配的 @PreAuthorize 场景返回 403（验证后可删除该测试接口）。
> 7. 编写单元测试：JwtUtils 生成-解析-过期校验；SecurityConfig 放行/拦截规则。
> 注意：本周不实现注册接口；不得在代码或日志中输出明文密码。
> 验收：401/403 返回体符合统一 Result 结构；token 30 分钟过期逻辑可测。

---

### W06 操作审计日志 AOP 组件（FR-022 底座）

**前置依赖**：W04、W05
**任务简述**：实现审计注解 + 切面，自动记录登录/增删改/审批等关键操作到 sys_audit_log，敏感参数脱敏。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：在 `backend/` 中实现操作审计日志组件（对应 FR-022、表 sys_audit_log），供本周及后续所有模块复用。
> 要求：
> 1. 自定义注解 `@OperLog`（属性：module 模块编码如 M01/M10、operation 操作类型 LOGIN/LOGOUT/ADD/UPDATE/DELETE/QUERY_SENSITIVE、description 操作描述），标注在 Controller 方法上。
> 2. `AuditLogAspect`（AOP 环绕通知）：自动采集——操作人账号/姓名（从 SecurityUtils 获取，未登录场景如登录失败也要能记录 username 入参）、requestUri、HTTP method、请求参数（JSON 序列化后**必须脱敏**：password/oldPassword/newPassword/initialPassword/confirmPassword 等字段一律输出 `******`）、客户端 IP、耗时 cost_time、成功/失败状态（status 1/0）、失败时 error_msg；异步或同步保存至 sys_audit_log（同步即可，保证事务内可见）。
> 3. `AuditLogService` + 实现：提供 `record(...)` 内部方法与分页查询方法（查询接口在后续任务实现，本任务先写保存能力）。
> 4. 日志内容不得包含明文密码、完整身份证号；日志表数据业务用户不可改删（本任务不提供任何修改/删除审计日志的接口）。
> 5. 编写单元测试：模拟一个标注 @OperLog 的测试接口，调用后验证 sys_audit_log 新增记录且参数字段已脱敏；异常路径验证 status=0 与 error_msg 落库。
> 验收：切面可被任意 Controller 方法复用；密码字段在日志中为掩码。

---

## 阶段三：公共模块后端业务（W07~W11）

### W07 登录认证后端（API-001 登录 / API-002 登出 / API-004 当前用户信息 / API-010 当前用户菜单树）

**前置依赖**：W05、W06、W02
**任务简述**：实现登录（含失败 5 次锁定 15 分钟、停用拦截）、登出、获取当前用户信息、获取当前用户菜单树。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现 UC-001 用户登录相关后端接口（对应详细设计 API-001、API-002、API-004、API-010）。
> 要求：
> 1. `AuthController` + `LoginService`/实现类，接口路径与出入参严格按详细设计说明书：
>    - `POST /api/auth/login`：入参 username/password；出参 token、userId、username、realName、roleCode、roleName、menus（菜单树）。
>    - `POST /api/auth/logout`：登出（记录登出日志）。
>    - `GET /api/auth/user-info`：返回当前登录用户信息（含 employeeId，本周普通职工未绑定时为 null）。
>    - `GET /api/menus/current`：按当前用户角色返回可见菜单树（只含 type=1/2 且 status=1 的节点，按 sort 排序，children 递归）。
> 2. 登录逻辑严格按详细设计 §3.1 流程与 PRD UC-001：
>    - 账号不存在或密码错误：统一提示"账号或密码错误"；
>    - 密码错误：failed_attempts +1，达到 5 次设置 locked_until = 当前时间 +15 分钟；
>    - 锁定中（locked_until 未到）：提示"账号已锁定，请 15 分钟后重试"；
>    - status=0 停用：提示"账号已停用，请联系系统管理员"；
>    - 成功：BCrypt 比对通过、清零 failed_attempts、更新 last_login_time/last_login_ip、签发 JWT；
>    - 登录成功/失败均通过 @OperLog(module="M01", operation="LOGIN") 记录审计。
> 3. 密码使用 BCryptPasswordEncoder 比对；登录 DTO 加 @Valid 校验（非空）。
> 4. 菜单树构建：sys_user.role_id → sys_role_permission → sys_permission，组装树形 VO（id/name/path/icon/parentId/sort/children）。
> 5. 编写 JUnit 5 单元测试覆盖：正确登录成功返回 token；密码错误累计 5 次锁定；锁定期间拒绝；停用账号拒绝；菜单树仅返回授权节点。
> 注意：不实现注册接口；不实现人事业务任何接口。
> 验收：Apifox/Postman 验证 API-001/002/004/010 全部分支与详细设计一致。

---

### W08 角色权限管理后端（API-011~API-017）

**前置依赖**：W04、W05
**任务简述**：角色列表/新增/修改/删除、全量权限树、查询角色权限、为角色分配权限。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现 UC-004 分配角色与权限的后端接口（对应详细设计 API-011~API-017），全部仅系统管理员可访问（@PreAuthorize("hasRole('ADMIN')")）。
> 要求：
> 1. `RoleController`、`PermissionController` + 对应 Service/实现，路径与出入参严格按详细设计：
>    - `GET /api/roles`：角色列表（roleId、roleCode、roleName、description、userCount 关联用户数）；
>    - `POST /api/roles`：新增角色（roleCode 唯一校验）；
>    - `PUT /api/roles/{id}`：修改角色（roleCode 不可改）；
>    - `DELETE /api/roles/{id}`：删除角色——仍被 sys_user 引用时拒绝，提示"该角色仍被用户使用，请先调整用户角色"；
>    - `GET /api/permissions/tree`：全量权限/菜单树（含 type=3 按钮权限点，供角色权限配置页勾选）；
>    - `GET /api/roles/{id}/permissions`：返回角色已分配权限 ID 数组；
>    - `PUT /api/roles/{id}/permissions`：覆盖式分配权限（入参 permissionIds），至少保留 1 个菜单权限否则提示"角色至少需要一个可用菜单权限"；保存后写 @OperLog(module="M10", operation="UPDATE", description="角色权限变更")。
> 2. 角色删除与权限分配为事务操作（@Transactional）；权限分配采用"先删后插"sys_role_permission。
> 3. DTO 加 @Valid 校验；业务错误抛 BusinessException 由全局异常处理器统一返回。
> 4. 编写单元测试：新增角色 roleCode 重复被拒；删除被引用角色被拒；权限分配后查询回显一致；非 ADMIN 角色访问返回 403。
> 注意：内置 ADMIN/HR/EMPLOYEE 三个角色允许查询但不允许删除（删除内置角色提示"内置角色不可删除"）。
> 验收：API-011~017 全部可通过 Apifox 验证，行为与 PRD UC-004 异常流一致。

---

### W09 用户账号管理后端（API-005~API-009）

**前置依赖**：W08（新增账号需角色列表/角色校验）
**任务简述**：管理员新增账号、账号分页查询、修改账号、停用/启用、重置密码。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现 UC-003 管理系统用户账号的后端接口（对应详细设计 API-005~API-009），全部仅系统管理员可访问。
> 要求：
> 1. `UserAccountController` + `UserAccountService`/实现，路径与出入参严格按详细设计：
>    - `POST /api/admin/accounts`：新增账号（username 唯一校验、初始密码 BCrypt 加密、roleId 必须存在；同步向 sys_password_history 写入初始密码一条；employeeId 本周可空、不校验 employee 表）；
>    - `GET /api/admin/accounts`：分页列表（默认每页 20），支持 username/realName 模糊、roleId、status 筛选；返回 roleName、lastLoginTime、createTime；
>    - `PUT /api/admin/accounts/{id}`：修改账号（realName/roleId/remark，username 不可改）；
>    - `PUT /api/admin/accounts/{id}/status`：停用/启用——**禁止停用当前登录账号**（与 SecurityUtils 当前 userId 比对，提示"不能停用当前登录账号"）；
>    - `PUT /api/admin/accounts/{id}/reset-password`：重置密码（BCrypt 加密、写入密码历史）。
> 2. 所有写操作加 @OperLog(module="M01", operation=ADD/UPDATE)；参数 @Valid 校验；账号名重复提示"账号名已存在"。
> 3. 分页使用 MyBatis-Plus 分页插件，返回详细设计约定的分页结构（records/total/current/size）。
> 4. 编写单元测试：新增账号成功且密码为密文、username 重复被拒；停用当前登录账号被拒；重置密码后新密码可登录、旧密码失效；非管理员访问返回 403。
> 注意：不实现注册接口；本任务不做 employee_id 绑定选择器（字段保留可空）；不实现人事业务接口。
> 验收：Apifox 验证 API-005~009 全部分支，与 PRD UC-003 主成功场景/异常流一致。

---

### W10 修改本人密码后端（API-003）

**前置依赖**：W04、W05
**任务简述**：实现本人修改密码：旧密码校验、密码策略、最近 3 次历史密码校验、改后强制重新登录。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现 UC-002 修改个人密码后端接口（对应详细设计 API-003 `POST /api/auth/change-password`），三类角色登录后均可调用。
> 要求：
> 1. 入参 oldPassword、newPassword、confirmPassword（@Valid 非空）；逻辑严格按 PRD UC-002 与 FR-004：
>    - 旧密码 BCrypt 比对失败：提示"旧密码错误"；
>    - 两次新密码不一致：提示"两次输入的新密码不一致"；
>    - 新密码策略：长度 ≥8 位且必须包含字母与数字，否则提示具体规则；
>    - 新密码不得与最近 3 次历史密码（sys_password_history，按 create_time 倒序取 3 条 + 当前密码）相同，否则提示"新密码不能与最近 3 次使用过的密码相同"；
> 2. 校验通过：更新 sys_user.password（BCrypt）、向 sys_password_history 插入新密码记录、@OperLog(module="M01", operation="UPDATE", description="修改本人密码")；返回成功并在 message 中提示"密码修改成功，请重新登录"。
> 3. 改密成功后当前令牌失效：返回标识让前端清除 token 跳登录页（后端可在响应 data 中返回 needReLogin=true）。
> 4. 编写单元测试覆盖：旧密码错误、弱密码、两次不一致、与历史密码重复、成功后历史表新增且可用新密码登录。
> 验收：全部分支与 PRD UC-002 异常流逐条对应。

---

### W11 操作日志分页查询后端（API-018）+ 数据字典查询（API-019）

**前置依赖**：W06、W04
**任务简述**：管理员分页查询操作审计日志（多条件）；公共字典按类型查询接口（供后续模块下拉使用）。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现 FR-022 操作日志查询接口（API-018）与数据字典查询接口（API-019）。
> 要求：
> 1. `AuditLogController`（仅 ADMIN）：`GET /api/audit-logs`，分页查询 sys_audit_log，支持 username、module、operation、startTime、endTime 条件；返回字段严格按详细设计（logId、username、realName、module、operation、description、requestUri、method、ip、costTime、createTime）；默认按 create_time 倒序；**不提供日志的修改/删除接口**。
> 2. `DictController`（登录用户均可访问）：`GET /api/dicts?dictType=xxx`，按 dict_type 查询 status=1 的字典项，按 sort 排序，返回 dictId、dictLabel、dictValue、sort；本周验证 ATTENDANCE_STATUS、LEAVE_TYPE 等 W02 种子数据可查。
> 3. 两个接口均加 @OperLog 或按需要（日志查询可记 QUERY 类，字典查询不记）；参数校验与分页结构遵循统一约定。
> 4. 编写单元测试：日志多条件组合分页结果正确；非管理员访问日志接口 403；字典接口按类型返回正确项数。
> 验收：Apifox 验证 API-018/019；日志中登录、增删改等记录可在列表中检索到。

---

## 阶段四：公共模块前端页面（W12~W17）

### W12 登录页与前端鉴权联通

**前置依赖**：W07、W03
**任务简述**：实现登录页、user store 登录动作、Axios/路由守卫联通、动态菜单路由、按角色跳转。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现前端登录页与完整鉴权联通（对应详细设计 §2.1 登录页、§3.1 登录流程；接口 API-001/002/004/010）。
> 要求：
> 1. `src/views/login/index.vue`：居中卡片布局，系统标题"企业职工人事在线管理信息系统"；账号、密码输入框（Element Plus Form，必填校验）；【登录】【重置】按钮；错误提示红色就近显示；登录中 loading 状态。
> 2. 完善 `src/store/user.js`（Pinia）：login  action 调 `POST /api/auth/login`，token 存 localStorage 并持久化；getUserInfo 调 `/api/auth/user-info` 与 `/api/menus/current` 存储用户信息与菜单树；logout 调 `/api/auth/logout` 后清除全部登录态。
> 3. 路由守卫（`src/router/index.js`）：无 token 访问受保护路由 → 跳 /login；已登录访问 /login → 跳首页；登录后根据后端返回 menus 动态注册路由（addRoute），菜单 path 与组件映射；刷新页面时若 token 存在则重新拉取 userInfo/menus 恢复路由。
> 4. 登录成功按 roleCode 跳转：ADMIN → /home（系统管理工作台）；HR/EMPLOYEE → /home（本周仅首页占位）。
> 5. Axios 拦截器联通：401 自动清登录态跳登录页；登录失败（账号或密码错误/锁定/停用）在登录页 ElMessage 展示后端 message。
> 6. 本周不实现注册页；不实现人事业务页面。
> 验收：浏览器完成 admin/YOUR_ADMIN_PWD 登录→跳转首页；错误密码有提示；刷新页面保持登录态；退出后回到登录页。

---

### W13 系统管理主框架布局（Layout）

**前置依赖**：W12
**任务简述**：实现后台主框架：左侧动态菜单树、顶部导航（面包屑/用户姓名/角色/修改密码/退出）、主内容区。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现后台主框架布局页（对应详细设计 §2.2 首页/主框架），作为所有系统内页面的容器。
> 要求：
> 1. `src/views/layout/index.vue`：经典后台布局——左侧 el-menu 侧边栏（依据 user store 的 menus 动态渲染，支持目录/菜单两级、图标、折叠）；顶部导航栏含面包屑（跟随路由）、当前用户 realName 与角色名标签、下拉菜单【修改密码】【退出登录】；右侧主内容区 `<router-view/>`。
> 2. 菜单点击路由跳转；菜单组件与动态路由 path 一致；无权限的路由直接输入 URL 时（后端会 403/401）前端给出提示。
> 3. `src/views/home/index.vue` 工作台首页：欢迎语 + 当前用户信息卡片（姓名、角色、最后登录时间）；ADMIN 显示快捷入口卡片（账号管理、角色管理、操作日志），点击跳转对应页面；HR/EMPLOYEE 本周仅显示欢迎语（人事快捷入口待后续模块）。
> 4. 退出登录：确认弹窗 → 调 logout → 清 token → 跳登录页。
> 5. 闲置 30 分钟令牌过期后，任意请求返回 401 时拦截器自动跳登录页（依赖 W03 拦截器，本任务验证联通）。
> 验收：登录后左侧菜单与 ADMIN 种子权限一致（系统管理：账号管理/角色管理/操作日志）；面包屑、退出、快捷入口均可用。

---

### W14 角色权限配置页

**前置依赖**：W13、W08
**任务简述**：实现角色权限配置页（左侧角色列表 + 右侧权限树勾选 + 保存授权），对应详细设计 §2.7。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现角色权限配置页面（对应详细设计 §2.7；接口 API-011~API-017）。
> 要求：
> 1. `src/views/system/role/index.vue`：左右两栏布局。
>    - 左侧角色列表卡片：展示 roleName、roleCode、description、userCount；顶部【新增角色】按钮；卡片/行操作【编辑】【删除】【分配权限】；内置角色（ADMIN/HR/EMPLOYEE）禁用删除按钮。
>    - 右侧权限树：el-tree 带复选框（父子联动），数据来自 `GET /api/permissions/tree`，按模块分组显示目录/菜单/按钮三级；选中某角色后调 `GET /api/roles/{id}/permissions` 回显勾选；右上角【保存权限分配】按钮调 `PUT /api/roles/{id}/permissions`。
> 2. 新增/编辑角色弹窗：角色名称、角色编码（新增可填、编辑禁用）、描述；表单校验必填。
> 3. 删除角色：二次确认；后端返回"角色仍被用户使用"时 ElMessage 展示。
> 4. 保存授权：二次确认 → 成功提示"保存成功"；提示"角色至少需要一个可用菜单权限"等后端校验信息原样展示。
> 5. 页面按钮权限可依据权限点（role:add/role:edit/role:delete/role:assign）控制显隐（仅前端体验控制）。
> 验收：选中 ADMIN 角色能看到 W02 种子权限全部勾选；取消若干按钮权限保存后重新进入回显正确；删除被引用角色被拦截。

---

### W15 用户账号管理页

**前置依赖**：W13、W09、W08（角色下拉数据）
**任务简述**：实现账号管理页（搜索 + 分页表格 + 新增/编辑/停用启用/重置密码），对应详细设计账号管理交互。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现系统用户账号管理页面（接口 API-005~009，角色下拉用 API-011）。
> 要求：
> 1. `src/views/system/account/index.vue`：
>    - 顶部搜索区：账号、姓名输入框，角色下拉（数据来自 `GET /api/roles`），状态下拉（启用/停用），【查询】【重置】按钮；
>    - 工具栏【新增账号】按钮（权限点 account:add 控制显隐）；
>    - el-table 分页表格（默认 20 条）：账号、姓名、角色名、状态（标签）、最后登录时间、创建时间、操作列；
>    - 操作列：【编辑】【停用/启用】（切换文案）【重置密码】。
> 2. 新增账号弹窗：账号、初始密码、确认密码、姓名、角色（下拉必选）、备注；前端校验必填、密码长度≥8 含字母数字；提交调 `POST /api/admin/accounts`；账号重复等后端错误原样提示。
> 3. 编辑账号弹窗：账号输入框禁用（不可改），可改姓名、角色、备注；调 `PUT /api/admin/accounts/{id}`。
> 4. 停用/启用：二次确认调 `PUT /api/admin/accounts/{id}/status`；当前登录账号的停用按钮禁用或后端拦截后展示"不能停用当前登录账号"。
> 5. 重置密码：弹窗输入新密码（校验同密码策略）→ 调 `PUT /api/admin/accounts/{id}/reset-password`。
> 6. 所有操作成功后刷新当前页列表；操作中按钮 loading 防重复提交。
> 注意：本页面仅系统管理员可见（菜单与路由权限受控）；不做账号自助注册；employee_id 绑定本期不做。
> 验收：完成账号新增→查询→编辑→停用→重置密码全流程走查；与 PRD UC-003 主成功场景一致。

---

### W16 修改密码弹窗（个人中心）

**前置依赖**：W13、W10
**任务简述**：实现顶栏下拉中的修改密码弹窗，改密成功强制退回登录页。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现修改本人密码前端弹窗（接口 API-003，三类角色通用）。
> 要求：
> 1. 在 Layout 顶栏用户下拉中点击【修改密码】打开弹窗组件 `src/components/ChangePasswordDialog.vue`：旧密码、新密码、确认新密码三个密码框；前端校验——必填、新密码 ≥8 位且含字母和数字、两次输入一致；字段下方红色就近提示。
> 2. 提交调 `POST /api/auth/change-password`；后端错误（旧密码错误、与最近 3 次历史密码相同、策略不满足）原样 ElMessage 展示。
> 3. 成功后提示"密码修改成功，请重新登录"，清除 token 与用户状态，跳转登录页。
> 4. 弹窗关闭/取消时清空表单。
> 验收：admin 登录后修改密码→被强制登出→用新密码可登录、旧密码登录失败；弱密码/错误旧密码被拦截并提示。

---

### W17 操作日志查询页

**前置依赖**：W13、W11
**任务简述**：实现操作审计日志分页查询页（多条件搜索 + 表格），对应 FR-022。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：实现操作日志查询页面（接口 API-018），仅系统管理员可见。
> 要求：
> 1. `src/views/system/audit/index.vue`：
>    - 搜索区：操作人账号、模块（下拉 M01~M10 中文映射）、操作类型（下拉 LOGIN/LOGOUT/ADD/UPDATE/DELETE/QUERY_SENSITIVE 中文映射）、操作时间范围（el-date-picker 起止）；【查询】【重置】；
>    - el-table 分页表格列：操作人（账号/姓名）、模块、操作类型（标签）、操作描述、请求 URI、HTTP 方法、IP、耗时(ms)、状态（成功/失败标签）、操作时间；
>    - 默认按操作时间倒序，分页 20 条。
> 2. 点击行可展开查看 request_param/error_msg 详情（el-table 展开行或详情弹窗）；密码等敏感字段后端已脱敏，前端原样展示。
> 3. 页面不提供任何日志修改/删除入口。
> 验收：登录、登出、账号增改、权限变更、改密等操作后，日志列表可按条件检索到对应记录且信息完整。

---

## 阶段五：联调验收（W18）

### W18 公共模块端到端联调与冒烟测试

**前置依赖**：W01~W17 全部完成
**任务简述**：前后端联调，按 UC-001~004 与 FR-022 执行全链路冒烟走查，输出 Apifox 接口集合与缺陷清单。

**独立提示词**：

> 参考本仓库docs目录PRD、概要设计说明书.md、详细设计说明书.md、数据库设计文档.md、测试方案与用例集.md、PROJECT.md，严格按照文档内UC用例、需求、接口、表结构进行开发。
>
> 任务：对本周公共模块进行端到端联调与冒烟测试（本任务只做验证与缺陷修复，不新增功能、不设计新内容）。
> 要求：
> 1. 后端启动（8080）、前端启动（5173，代理 /api）、MySQL eopmis 库就绪，使用 W02 种子数据（admin/YOUR_ADMIN_PWD）。
> 2. 在 Apifox/Postman 中建立本周接口集合（API-001~API-019 已实现部分），逐条验证：登录成功/失败/锁定/停用、登出、user-info、菜单树、角色 CRUD、权限树/授权、账号 CRUD/停用/重置密码、修改密码全分支、日志分页、字典查询；导出集合文件放入 `docs/test/`（或项目约定目录）。
> 3. 浏览器端走查 PRD 用例主成功场景：
>    - UC-001 登录（含 5 次错误锁定 15 分钟、停用账号提示）；
>    - UC-002 修改密码（旧密码校验、策略、3 次历史、强制重新登录）；
>    - UC-003 账号管理（新增 HR 账号、查询、编辑、停用当前账号被拦截、重置密码后新密码可登录）；
>    - UC-004 角色权限（新建角色→授权→用该角色账号登录验证菜单变化；删除被引用角色被拦截）；
>    - FR-022 审计（上述全部操作在操作日志页可检索，且无密码明文）。
> 4. 安全检查：未登录访问受保护接口返回 401；HR/EMPLOYEE 角色访问 /api/admin/** 与 /api/roles 写接口返回 403；直接篡改 URL/参数越权访问被后端拦截。
> 5. 对走查发现的缺陷进行修复（前后端均在本任务内闭环），修复后回归；输出一份《公共模块冒烟测试记录》（通过/失败项清单 + 缺陷简述）。
> 注意：不得顺手实现人事业务（部门/岗位/员工/合同/调动/薪资/考勤/请假）任何功能。
> 验收：UC-001~004 主成功场景与关键异常流全部通过；P0/P1 缺陷清零；接口集合与测试记录归档。

---

## 任务依赖关系一览

```
W01 后端脚手架 ──┬──> W04 实体/Mapper ──> W05 Security/JWT ──> W06 审计AOP
W02 数据库初始化 ┘           │                    │
W03 前端脚手架（可并行）       ├──────────┬─────────┴──> W07 登录后端 ──> W12 登录页/鉴权联通 ──> W13 Layout
                             ├─> W08 角色权限后端 ──┐                              ├─> W14 角色权限配置页
                             └─> W04/W05 ─> W10 改密后端                          ├─> W15 账号管理页（依赖W08）
W09 账号管理后端（依赖 W08）──────────────────────────┘                              ├─> W16 修改密码弹窗（依赖W10）
W11 日志/字典后端（依赖 W06/W04）─────────────────────────────────────────────────┴─> W17 操作日志页
W01~W17 全部完成 ──> W18 端到端联调与冒烟测试
```

| 阶段 | 任务 | 覆盖需求 |
|------|------|----------|
| 一、工程底座 | W01~W03 | 公共基础 |
| 二、后端公共组件 | W04~W06 | 公共基础、FR-022 底座 |
| 三、后端业务 | W07 | UC-001、FR-003 |
|  | W08 | UC-004、FR-002 |
|  | W09 | UC-003、FR-001 |
|  | W10 | UC-002、FR-004 |
|  | W11 | FR-022、字典公共服务 |
| 四、前端页面 | W12~W17 | 上述 UC 的页面实现 |
| 五、联调验收 | W18 | UC-001~004、FR-022 全链路 |

> 人事业务模块（M02 员工档案、M03 部门、M04 岗位、M05 合同、M06 调动、M07 薪资、M08 考勤、M09 请假审批）在本周公共模块全部验收通过后，于下一期 WBS 按"部门岗位 → 员工档案 → 合同/调动 → 考勤/请假 → 薪资"顺序排期。

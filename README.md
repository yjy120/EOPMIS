# EOPMIS 企业职工人事在线管理信息系统

> Enterprise Online Employee HR Management Information System
> 基于 Spring Boot + Vue 3 的前后端分离人事管理平台，覆盖人事业务全链路与 RBAC 权限体系

| Spring Boot 2.7 | Vue 3 | MyBatis-Plus | MySQL 8.0 | Security + JWT |
|---|---|---|---|---|

## 一、项目概述

随着企业规模扩大、人员流动频繁，传统依赖 Excel 与纸质档案的人事管理方式存在**数据分散、查询统计效率低、档案易丢失、权限管控混乱**等问题。EOPMIS 是一套 B/S 架构的人事综合管理平台，覆盖部门岗位、职工档案、劳动合同、人事调动、薪资、考勤、账号权限等核心业务。

系统设置 **系统管理员（ADMIN）/ 人事专员（HR）/ 普通职工（EMPLOYEE）** 三类角色，实现细粒度权限隔离，对身份证、薪资等敏感信息按角色控制可见范围，所有人事变更操作留痕审计，满足个人信息保护合规要求。

**项目规模**：13 个数据库实体、11 个业务模块、约 70+ RESTful 接口、完整的权限与审计链路。

## 二、核心功能

按业务域划分，每个模块均包含完整的增删改查与审计：

| 业务域 | 主要功能 |
|---|---|
| 认证与账号 | 登录、验证码、修改密码、账号创建/停用/重置密码、密码历史校验 |
| 权限体系 | 角色管理、菜单与权限分配（RBAC）、`@PreAuthorize` 方法级鉴权 |
| 组织架构 | 部门树（支持层级）、岗位管理 |
| 职工档案 | 档案录入/查询/修改、本人档案自助查看、入职/离职状态管理 |
| 劳动合同 | 合同签订、终止、到期查询 |
| 人事调动 | 部门/岗位调动记录 |
| 薪资管理 | 薪资月份记录、构成项（基本/岗位/绩效/津贴/扣款）与实发工资 |
| 考勤管理 | 考勤记录、状态字典 |
| 审计日志 | 关键操作全量留痕（操作人、IP、模块、动作、时间） |

三类角色的权限边界：

- **ADMIN**：账号、角色、字典、审计配置，**不接触人事业务数据**。
- **HR**：部门、档案、合同、调动、薪资、考勤全业务权限。
- **EMPLOYEE**：仅可查看**本人**档案/合同/薪资/考勤，仅允许修改自身密码。

## 三、技术架构

```
浏览器 (Vue 3 + Vite + Element Plus + Pinia + Axios)
        │  HTTP / JSON（Bearer JWT）
        ▼
Spring Boot 后端（无状态）
  ├─ SecurityConfig       安全过滤链（STATELESS，401/403 统一 JSON）
  ├─ JwtAuthFilter        解析 Token → 写入 SecurityContext
  ├─ Controller 层        @PreAuthorize 角色拦截 + @Valid 参数校验
  ├─ Service 层           业务逻辑 + 事务 + 审计写入
  ├─ Mapper 层            MyBatis-Plus + XML 自定义查询
  └─ MySQL 8.0            Druid 连接池 + 逻辑删除
```

后端采用标准分层：`controller → service → mapper → entity`，配合 `config / security / dto / common / util` 公共层。

**技术选型**

- **后端**：Java 17、Spring Boot 2.7.18、Spring MVC、MyBatis-Plus 3.5.5、Spring Security、JWT（jjwt 0.11.5）、Druid、MySQL 8.0、Hutool、Lombok、Knife4j
- **前端**：Vue 3、Vite 5、Element Plus、Pinia、Vue Router、Axios、ECharts
- **工程质量**：统一响应体 `Result<T>`、`BizException` 业务异常、全局异常处理器、参数校验、逻辑删除

## 四、技术亮点

**1. 无状态 JWT 鉴权 + 方法级权限控制**
自研 `JwtAuthFilter`（继承 `OncePerRequestFilter`）从 `Authorization: Bearer` 解析 Token，提取 `uid/role` 写入 `SecurityContext`；解析失败不抛异常，交由后续鉴权统一返回 401。`SecurityConfig` 关闭 Session（`STATELESS`），放行登录与 Swagger，其余接口强制认证，并用 `@EnableGlobalMethodSecurity(prePostEnabled=true)` 开启方法级角色拦截。401/403 均返回统一 JSON，而非 Spring 默认错误页。

**2. 登录安全：防枚举 + 失败锁定 + 审计**
账号不存在与密码错误返回**同一提示**，防止账号枚举；连续失败达到阈值（默认 5 次）自动锁定账号 15 分钟；每次登录尝试（成功/失败/锁定）均写入 `sys_audit_log`，记录操作人、IP、动作与结果。

**3. 密码安全策略**
BCrypt 不可逆加密存储（`PasswordUtil`）；密码强度校验（长度、字母、数字）；保留最近 N 次历史密码，禁止重复使用；`DataInitRunner` 在启动时幂等初始化种子账号，避免重复插入。

**4. 防越权设计**
`/me/*` 系列接口（本人档案/合同/薪资/考勤）**不接受任何 userId 入参**，一律从 JWT 中的 `uid` 反查 `sys_user.employee_id`，调用方无法伪造他人 ID；未绑定员工号时返回 404 而非空列表。

**5. 审计日志贯穿业务层**
所有写操作（档案、合同、调动、薪资、考勤、账号）在 Service 层统一调用 `writeAudit()`，实现人事变更全链路可追溯。

## 五、目录结构

```
EOPMIS/
├── backend/
│   └── src/main/
│       ├── java/com/eopmis/
│       │   ├── common/       # Result / BizException / GlobalExceptionHandler
│       │   ├── config/       # Security / Cors / MybatisPlus / DataInitRunner
│       │   ├── controller/   # 13 个 Controller（auth / hr / me / admin ...）
│       │   ├── dto/          # 请求与视图对象（含冗余展示字段）
│       │   ├── entity/       # 13 个实体
│       │   ├── mapper/       # MyBatis-Plus Mapper + 自定义 XML
│       │   ├── security/     # JwtAuthFilter / SecurityConfig / CurrentUser
│       │   ├── service/      # 11 个业务接口 + impl
│       │   └── util/         # JwtUtil / PasswordUtil
│       └── resources/        # application.yml / mapper XML / sql
├── frontend/                 # Vue 3 前端
│   └── src/                  # api / router / store / utils / views
└── docs/                     # 需求、设计、测试与开发文档
```

## 六、快速开始

**环境要求**：JDK 17、Maven 3.6+、MySQL 8.0、Node.js 16+

```bash
# 1. 建库并导入表结构与种子数据
mysql -u root -p < backend/src/main/resources/sql/init.sql

# 2. 配置数据库连接（application.yml），密码通过本地配置或环境变量注入
cd backend && mvn spring-boot:run

# 3. 启动前端
cd frontend && npm install && npm run dev
```

- 后端：`http://localhost:8080`，接口前缀 `/api`
- 接口文档（Knife4j）：`http://localhost:8080/api/doc.html`
- 前端：`http://localhost:5173`

> 种子账号由 `DataInitRunner` 启动时幂等初始化；演示密码已替换为占位符，运行前请通过本地配置注入，**部署公开环境务必修改默认密码**。

## 七、成员

于悦、杨佳莹、王岩（生产实习三人小组项目，成员轮流承担开发角色）

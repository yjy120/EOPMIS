# EOPMIS 企业职工人事在线管理信息系统

> Enterprise Online Employee HR Management Information System · 基于 Spring Boot + Vue3 的前后端分离人事管理平台

## 项目简介

随着企业规模扩大、人员流动频繁，传统依赖 Excel 与纸质档案的人事管理方式存在数据分散、查询统计效率低、档案易丢失、权限管控混乱等问题。EOPMIS 是一套 B/S 架构的人事综合管理平台，覆盖部门岗位、职工档案、劳动合同、人事调动、薪资、考勤、账号权限等核心业务。

系统设置 **系统管理员 / 人事专员 / 普通职工** 三类角色，实现细粒度权限隔离，对身份证、薪资等敏感信息脱敏，所有人事变更操作留痕审计，满足个人信息保护合规要求。

## 核心功能

| 编号 | 用例 | 说明 |
|------|------|------|
| UC-001 | 用户登录 | JWT 鉴权、连续失败锁定、审计日志 |
| UC-002 | 修改密码 | 密码强度策略、历史密码校验 |
| UC-003 | 账号管理 | 管理员分页查询 / 新增 / 停用账号 |
| UC-004 | 角色权限 | 基于 RBAC 的角色与菜单权限管理 |

> 项目按 M01~M10 模块排期迭代，当前仓库已实现上述 4 个基础用例与配套权限体系，其余人事业务模块持续开发中。

## 技术栈

**后端**
- Java 17 / Spring Boot 2.7.18 / Spring MVC
- MyBatis-Plus 3.5.5 + Druid 连接池
- Spring Security + JWT（jjwt 0.11.5）
- MySQL 8.0 / Hutool / Lombok / Knife4j（接口文档）

**前端**
- Vue 3 + Vite 5
- Element Plus + ECharts + Pinia + Vue Router + Axios

## 目录结构

```
EOPMIS/
├── backend/                        # Spring Boot 后端工程
│   └── src/main/
│       ├── java/com/eopmis/        # controller / service / mapper / entity / security / config
│       └── resources/              # application.yml、mapper XML、sql/init.sql
├── frontend/                       # Vue 3 前端工程
│   └── src/                        # api / router / store / utils / views
├── docs/                           # 需求、设计、测试与开发文档
└── README.md
```

后端采用标准分层架构：`controller → service → mapper → entity`，配合 `config`、`security`、`dto`、`common` 公共层。

## 快速开始

### 1. 准备数据库

```bash
mysql -u root -p < backend/src/main/resources/sql/init.sql
```

### 2. 配置并启动后端

修改 `backend/src/main/resources/application.yml` 中的数据库连接用户名与密码（密码请用环境变量或本地配置注入，勿提交明文），然后：

```bash
cd backend
mvn spring-boot:run
```

- 服务地址：`http://localhost:8080`
- 接口前缀：`/api`
- 接口文档（Knife4j）：`http://localhost:8080/api/doc.html`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`。

> 默认种子账号（admin / hr01 / emp01）由后端 `DataInitRunner` 启动时初始化，初始密码请查看本地配置，**部署到公开环境前务必修改**。

## 角色说明

- **系统管理员**：负责账号创建与角色权限分配，不处理人事业务数据。
- **人事专员**：维护部门、职工档案、合同、调动、薪资考勤等业务。
- **普通职工**：仅可查看本人档案、合同、薪资考勤，仅允许修改自身登录密码。

## 成员

于悦、杨佳莹、王岩（生产实习三人小组项目，成员轮流承担不同开发角色）

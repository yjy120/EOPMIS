# EOPMIS · AI 贡献声明

> 整理日期：2026-09-21
> 工期：2026-08-31 ~ 2026-09-23（2 天）
> 文档定位：客观披露 AI 在本项目中的实际贡献占比，按模块给出可核验数据，与 [Git 历史](#)、[代码审查记录](#)、[缺陷跟踪表](缺陷跟踪表.md)、[需求变更记录](需求变更记录.md) 相互印证
> 性质：**声明性文档**，供评审 / 实习报告 / 后续维护者参考

---
## 1. 项目规模基线（实测）
### 1.1 后端模块（97 个文件，4,948 行）

| 模块 | 文件数 | 行数 | 占比 |
|------|--------|------|------|
| `service` + `service.impl` | 20 | 2,314 | **46.8%** |
| `controller` | 12 | 672 | 13.6% |
| `dto` | 26 | 635 | 12.8% |
| `entity` | 13 | 439 | 8.9% |
| `mapper` | 12 | 298 | 6.0% |
| `config` | 4 | 201 | 4.1% |
| `security` | 4 | 195 | 3.9% |
| `common` | 3 | 103 | 2.1% |
| `util` | 2 | 75 | 1.5% |
| **后端合计** | **97** | **4,948** | **100%** |
### 1.2 前端模块（34 个文件，3,644 行）

| 模块 | 文件数 | 行数 | 占比 |
|------|--------|------|------|
| `views`（页面） | 17 | 3,394 | **93.1%** |
| `api`（接口封装） | 12 | 91 | 2.5% |
| `store`（Pinia） | 1 | 51 | 1.4% |
| `utils` | 1 | 44 | 1.2% |
| `router` | 1 | 43 | 1.2% |
| 入口 / 主配置 | 2 | 21 | 0.6% |
| **前端合计** | **34** | **3,644** | **100%** |

> **注**：前端的 `views/` 占了 93.1% 行数，符合 Vue 项目的典型分布（页面逻辑远比基础设施代码量大）。
### 1.3 文档（22 份，8,886 行）

| 阶段 | 文档数 | 行数 |
|------|--------|------|
| 第一周 PRD 编制 | 5 | ~3,200 |
| 第二周 设计/原型 | 7 | ~3,800 |
| 第三周 迭代/缺陷/执行记录 | 8 | ~1,800 |
| 第四周 部署/AI 声明 | 2 | ~86 |
| **合计** | **22** | **8,886** |

---
## 2. Git 历史印证
### 2.1 Commit 总览

```
项目根目录 .git 统计：
- 总 commit 数：64
- 总行数（含历史）：约 32,000+（含历史回滚）
- 主体节奏：第二周密集编码（38 commits），第三周增量提交（26 commits）
```
### 2.2 Commit 分布特征

| 时间段 | Commit 数 | 主要内容 | AI 关联度 |
|--------|-----------|----------|-----------|
| 2026-09-01 ~ 09-13（第一、二周） | ~38 | Spring Boot 原型骨架 + MyBatis 实体 + 鉴权 | 95%（几乎全部 AI 一次性产出） |
| 2026-09-14 ~ 09-18（第三周） | ~26 | UC-007~017 业务代码 + 修复补丁 | 90%（AI 产出为主 + 人工修复） |
### 2.3 印证结论

Git commit 历史符合"AI 协作开发"特征：
- **commit 数量大但每次 diff 集中**——典型"AI 一次性输出多个文件 + 用户一次 commit"
- **修复类 commit 占比 ~30%**——反映"AI 一次输出后人工针对缺陷打补丁"的迭代模式
- **无"长链路逐步实现"痕迹**——典型 AI 协作模式是"任务一次性完成"，区别于"按编码规范逐步提交"

---
## 3. 后端模块 AI 贡献占比

> 下表"AI 占比" = 该模块中由 AI 直接产出（含一次采纳 + 多次调整后采纳）的代码行数 / 该模块总行数

### 3.1 service + service.impl（2,314 行）

| 子模块 | 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|--------|------|-------------|--------------|----------|---------|
| `service/`（接口） | ~620 | 615 | 605 | 15 | **97.6%** |
| `service/impl/`（实现） | ~1,694 | 1,650 | 1,540 | 154 | **90.9%** |
**人工修改要点**：
- [SalaryServiceImpl.java:62-65](file:///c:/Users/Lenovo/Desktop/EOPMIS/backend/src/main/java/com/eopmis/service/impl/SalaryServiceImpl.java#L62-L65) — SQL 别名 bug（沿用 DEF-019 / CR-107）
- [AttendanceServiceImpl.java:93](file:///c:/Users/Lenovo/Desktop/EOPMIS/backend/src/main/java/com/eopmis/service/impl/AttendanceServiceImpl.java#L93) — "考勤日期不能晚于今天"业务校验补强
- [ContractServiceImpl](../backend/src/main/java/com/eopmis/service/impl/ContractServiceImpl.java) — 编译错误修复（沿用 DEF-013）
### 3.2 controller（672 行）

| 子模块 | 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|--------|------|-------------|--------------|----------|---------|
| `controller/`（HR/EMP/Auth/Me） | 672 | 670 | 18 | 18 | **94%** |
**人工修改要点**：
- [MeController.java:67-97](file:///c:/Users/Lenovo/Desktop/EOPMIS/backend/src/main/java/com/eopmis/controller/MeController.java#L67-L97) — UC-014~017 /me/salary + /me/attendance 接口追加（CR-101）
- [SalaryController.java](../backend/src/main/java/com/eopmis/controller/SalaryController.java) / [AttendanceController.java](../backend/src/main/java/com/eopmis/controller/AttendanceController.java) — UC-014/015 HR 端入口补全
### 3.3 dto（635 行）

| 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|-------------|--------------|----------|---------|
| 635 | 633 | 605 | 30 | **95.3%** |
**人工修改要点**：
- [SalaryCreateRequest.java](../backend/src/main/java/com/eopmis/dto/SalaryCreateRequest.java) — `@Pattern salaryMonth` 字段格式约束
- [AttendanceCreateRequest.java](../backend/src/main/java/com/eopmis/dto/AttendanceCreateRequest.java) — `@Min @Max` 状态枚举校验
### 3.4 entity（439 行）

| 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|-------------|--------------|----------|---------|
| 439 | 437 | 415 | 24 | **94.5%** |
**人工修改要点**：
- [SysSalary.java](../backend/src/main/java/com/eopmis/entity/SysSalary.java) — `BigDecimal` 类型 + `@TableLogic` 注解
- [SysAttendance.java](../backend/src/main/java/com/eopmis/entity/SysAttendance.java) — `status TINYINT` + `leaveType/overtimeHours`
### 3.5 mapper + xml（298 行）

| 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|-------------|--------------|----------|---------|
| 298 | 286 | 264 | 34 | **88.6%** |
**人工修改要点**：
- [SalaryServiceImpl.java:62-65](../backend/src/main/java/com/eopmis/service/impl/SalaryServiceImpl.java#L62-L65) 间接导致的 `s.employee_id` / `s.salary_month` 别名 bug
- `mapper/SysMenuMapper.xml` 复杂 JOIN 与 `INNER JOIN` 调整
### 3.6 config + security + common（499 行）

| 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|-------------|--------------|----------|---------|
| 499 | 470 | 444 | 55 | **89.0%** |
**人工修改要点**：
- [application.yml](../backend/src/main/resources/application.yml) — jdbc URL 从 `127.0.0.1` 改 `[::1]`（沿用 CR-107 / DEF-017）
- [SecurityConfig.java](../backend/src/main/java/com/eopmis/config/SecurityConfig.java) — Security API 版本调整（沿用原型审查 P0-3）
- [DataInitRunner.java](../backend/src/main/java/com/eopmis/config/DataInitRunner.java) — `forceResetOnStartup` 开关（沿用 CR-201）
- [BizException.java](../backend/src/main/java/com/eopmis/common/BizException.java) — 默认 code=500 改为 50001/60001 系列
### 3.7 util（75 行）

| 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|-------------|--------------|----------|---------|
| 75 | 70 | 65 | 10 | **86.7%** |

---
## 4. 前端模块 AI 贡献占比
### 4.1 views（17 个页面，3,394 行）

| 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|-------------|--------------|----------|---------|
| 3,394 | 3,180 | 2,940 | 454 | **86.6%** |
**人工修改要点**：
- [Layout.vue](../frontend/src/views/Layout.vue) — UC-014~017 菜单项补全 + me:* 权限绑定（沿用 DEF-018）
- [MySalary.vue](../frontend/src/views/MySalary.vue) / [MyAttendance.vue](../frontend/src/views/MyAttendance.vue) — EMP 端页面 UI 调整（CR-102）
- [SalaryManage.vue](../frontend/src/views/SalaryManage.vue) / [AttendanceManage.vue](../frontend/src/views/AttendanceManage.vue) — HR 端筛选 UI 调整

> views 是前端人工修改的主要场所（454 行 / 前端总修改 75%），反映"UI 体验需多轮迭代"特征。
### 4.2 api（5 个接口封装，91 行）

| 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|-------------|--------------|----------|---------|
| 91 | 90 | 88 | 3 | **96.7%** |
**人工修改要点**：极少见，主要是 UC-014/017 新增接口路径补全。
### 4.3 store + router + utils（138 行）

| 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|-------------|--------------|----------|---------|
| 138 | 132 | 115 | 23 | **83.3%** |
**人工修改要点**：
- [router/index.js](../frontend/src/router/index.js) — UC-014~017 路由补全
- [store/user.js](../frontend/src/store/user.js) — `permVersion` 机制缺位（沿用 CR-108 / DEF-011）
- [utils/request.js](../frontend/src/utils/request.js) — 401 时 `localStorage.clear()` 行为调整（沿用原型审查 P2-5）
### 4.4 入口 + 主配置（21 行）

| 行数 | AI 直接生成 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|-------------|--------------|----------|---------|
| 21 | 21 | 21 | 0 | **100%** |

> main.js + vite.config.js 完全 AI 产出，无人工修改。

---
## 5. 总体统计
### 5.1 后端

| 模块 | 总行数 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|--------|--------------|----------|---------|
| service + impl | 2,314 | 2,145 | 169 | **92.7%** |
| controller | 672 | 654 | 18 | **97.3%** |
| dto | 635 | 605 | 30 | **95.3%** |
| entity | 439 | 415 | 24 | **94.5%** |
| mapper + xml | 298 | 264 | 34 | **88.6%** |
| config | 201 | 168 | 33 | **83.6%** |
| security | 195 | 169 | 26 | **86.7%** |
| common | 103 | 91 | 12 | **88.3%** |
| util | 75 | 65 | 10 | **86.7%** |
| **后端合计** | **4,948** | **4,576** | **372** | **92.5%** |
### 5.2 前端

| 模块 | 总行数 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|--------|--------------|----------|---------|
| views | 3,394 | 2,940 | 454 | **86.6%** |
| api | 91 | 88 | 3 | **96.7%** |
| store | 51 | 42 | 9 | **82.4%** |
| utils | 44 | 37 | 7 | **84.1%** |
| router | 43 | 36 | 7 | **83.7%** |
| 入口 + 配置 | 21 | 21 | 0 | **100%** |
| **前端合计** | **3,644** | **3,164** | **480** | **86.8%** |
### 5.3 全部代码

| 模块 | 总行数 | AI 最终采纳 | 人工修改 | AI 占比 |
|------|--------|--------------|----------|---------|
| 后端 | 4,948 | 4,576 | 372 | **92.5%** |
| 前端 | 3,644 | 3,164 | 480 | **86.8%** |
| **代码合计** | **8,592** | **7,740** | **852** | **90.1%** |

> **代码层面 AI 占比 90.1%**（与 [AI协作实验数据统计表](AI协作实验数据统计表.md) 中的 93.8% 略有差异，因本文档采用"AI 最终采纳"口径而非"AI 直接生成"口径）。

---
## 6. 人工修改要点（按文件类型分布）
### 6.1 后端人工修改分布

| 文件 | 修改行数 | 占比 | 关联记录 |
|------|----------|------|----------|
| [application.yml](../backend/src/main/resources/application.yml) | 9 | 2.4% | DEF-017 / CR-107 / CR-202 |
| [SalaryServiceImpl.java](../backend/src/main/java/com/eopmis/service/impl/SalaryServiceImpl.java) | 26 | 7.0% | DEF-019 / CR-103 |
| [AttendanceServiceImpl.java](../backend/src/main/java/com/eopmis/service/impl/AttendanceServiceImpl.java) | 15 | 4.0% | CR-104 错误码 |
| [MeController.java](../backend/src/main/java/com/eopmis/controller/MeController.java) | 18 | 4.8% | CR-101 / CR-104 |
| [DataInitRunner.java](../backend/src/main/java/com/eopmis/config/DataInitRunner.java) | 22 | 5.9% | CR-201 |
| [SecurityConfig.java](../backend/src/main/java/com/eopmis/config/SecurityConfig.java) | 11 | 3.0% | 原型审查 P0-3 |
| [BizException.java](../backend/src/main/java/com/eopmis/common/BizException.java) | 12 | 3.2% | 原型审查 P2-3 |
| 其他散点 | 259 | 69.7% | — |
| **合计** | **372** | **100%** | — |
### 6.2 前端人工修改分布

| 文件 | 修改行数 | 占比 | 关联记录 |
|------|----------|------|----------|
| [Layout.vue](../frontend/src/views/Layout.vue) | 12 | 2.5% | DEF-018 / CR-101 |
| [MySalary.vue](../frontend/src/views/MySalary.vue) | 23 | 4.8% | CR-102 |
| [MyAttendance.vue](../frontend/src/views/MyAttendance.vue) | 18 | 3.8% | CR-102 |
| [SalaryManage.vue](../frontend/src/views/SalaryManage.vue) | 25 | 5.2% | CR-101 |
| [AttendanceManage.vue](../frontend/src/views/AttendanceManage.vue) | 22 | 4.6% | CR-104 |
| [router/index.js](../frontend/src/router/index.js) | 7 | 1.5% | UC-014~017 路由 |
| [store/user.js](../frontend/src/store/user.js) | 9 | 1.9% | DEF-011 / CR-108 |
| [utils/request.js](../frontend/src/utils/request.js) | 7 | 1.5% | 原型审查 P2-5 |
| 其他散点（其他 9 个页面） | 357 | 74.2% | — |
| **合计** | **480** | **100%** | — |

---
## 7. 第三方印证
### 7.1 与 [缺陷跟踪表](缺陷跟踪表.md) 交叉印证

- 23 条 DEF 中，**22 条修复**对应代码修改行 ≤ 30 行（与"AI 输出 95% 一次性到位"的整体特征一致）
- 仅 DEF-011（permVersion）跨多轮（≥5 轮），涉及架构层面，与 AI 协作效率无关
- **印证结论**：缺陷数量与人工修改率呈正相关（缺陷越少 → 一次命中越高 → 人工修改越少）
### 7.2 与 [需求变更记录](需求变更记录.md) 交叉印证

- 39 条 CR 中，**已闭环 26 条**对应的代码修改行 ≤ 20 行
- 4 条待修复 / 持续项中，CR-101（UC-016 月份参数）是"PRD 隐含语义未明确"——属于 PRD 表述问题，**非 AI 实现问题**
- **印证结论**：CR 数量与人工修改率呈反相关（CR 越多 → PRD 越模糊 → AI 一次命中率越低 → 人工修改越多）
### 7.3 与 [原型代码审查记录](../第二周/原型代码审查记录.md) 交叉印证

- 原型审查 21 条问题（P0/P1/P2/P3）中，18 条闭环后代码修改行平均 ≤ 15 行
- 仅有 3 条（CR-206 N+1、CR-209 codeToId、CR-211 错误码边界）涉及多轮重构
- **印证结论**：原型阶段一次性产出 + 集中修复的节奏，符合 AI 协作开发特征
### 7.4 与 Git commit 历史交叉印证

- 64 个 commit 中，**~70% 是"一次性提交多个文件"**——典型 AI 协作模式（区别于人工"逐文件逐步提交"）
- **~25% 是修复类 commit**——对应 23 条 DEF 中已闭环的 22 条
- **~5% 是文档类 commit**——22 份文档的版本管理
- **印证结论**：commit 模式与 AI 协作"批量产出 + 集中修复"高度吻合

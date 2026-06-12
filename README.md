# 托管培训中心信息管理系统

数据库课程设计项目 | 前后端分离架构 | Spring Boot + Vue 3 + MySQL

---

## 一、项目简介

### 1.1 项目背景

某托管培训中心需要建立一套信息管理系统，替代原有的人工管理方式，对学生报名、科目维护、教室排课以及账目收费等日常业务进行统一的数字化管理。

### 1.2 核心业务需求

| 序号 | 业务模块 | 功能描述 |
|:----:|----------|----------|
| 1 | **学生报名** | 根据学生报名的科目查询班级信息；若满员则提示报名下期；未满员则选择教师（同科目不同教师收费不同），完成报名登记并开具收费清单 |
| 2 | **科目管理** | 维护科目基本信息（奥数、围棋、书法、口才等）；同一科目可由不同等级教师承担，收费标准与课时报酬因教师知名度而异 |
| 3 | **排课管理** | 为每个班级安排上课时间与教室，生成学生个人课表与教师授课日程表 |
| 4 | **账目管理** | 记录每笔缴费流水，支持开具收据、打印收费清单、催缴欠费查询 |

### 1.3 数据要求

- **学生信息**：学生编号、学生姓名、报名时间、交款额、所选科目（可能不止一门）
- **科目信息**：科目号、科目名、学时、上课周期、收费、上课地点、教师号、招收人数、已报名人数
- **教师信息**：教师号、教师名、教师等级、教师特长
- **账目信息**：日期、班级代号、学生编号、科目号、交款额

---

## 二、技术栈

| 技术/工具 | 类别 | 版本 | 作用 |
|-----------|------|------|------|
| Java | 后端语言 | JDK 17 (Temurin) | 编写业务逻辑：学生报名、收费计算、满员判断等 |
| Spring Boot | 后端框架 | 3+ | 快速搭建后端服务器，提供 REST API |
| MyBatis | ORM 框架 | 3+| 后端与 MySQL 数据交互，注解 + Mapper 混合 |
| Maven | 构建工具 | 3+| 依赖管理与项目构建 |
| Vue.js | 前端框架 | 3+ (Composition API) | 构建用户操作界面 |
| Vite | 前端构建 | 5+ | Vue 项目开发服务器与生产打包 |
| Vue Router | 前端路由 | 4+ | 单页面应用路由管理 |
| Axios | HTTP 库 | 最新 | 前后端数据通信 |
| Element Plus | UI 组件库 | 最新 | 表格、表单、弹窗、导航等 UI 组件 |
| Pinia | 状态管理 | 最新 | 登录用户身份持久化、角色状态管理 |
| Node.js | 前端运行环境 | v24.16.0 | Vue 项目运行与构建依赖 |
| npm | 包管理器 | 11.13.0 | 前端依赖安装与管理 |
| MySQL | 数据库 | 8+| 持久化存储全部业务数据 |
| MySQL Workbench | 数据库工具 | — | 可视化管理、建表、调试 SQL |
| IntelliJ IDEA | 后端 IDE | — | Spring Boot 项目开发与调试 |
| VS Code | 前端编辑器 | — | Vue 项目开发 |
| Postman | 接口调试 | — | 独立测试后端 API |
| Git + GitHub | 版本控制 | — | 代码版本管理、每日提交、远程托管 |

---

## 三、系统架构

### 3.1 整体架构

```
浏览器 (Browser)  ─── http://localhost:5173 ───▶  前端 (Vue 3 + Element Plus + Pinia)
                                                  ├─ Router       路由层（beforeEach 角色守卫）
                                                  ├─ Stores       状态层（auth 用户身份持久化）
                                                  ├─ Views        视图层（8 个页面组件）
                                                  ├─ Components   组件层（SidebarNav 角色导航）
                                                  └─ API          Axios 请求封装层（8 个模块）

                                                     │  HTTP/JSON (CORS 跨域)
                                                     ▼
                                                 后端 (Spring Boot + MyBatis)
                                                  ├─ Controller  控制层（8 个 REST 接口）
                                                  ├─ Service     业务逻辑层（事务管理/校验/认证）
                                                  ├─ Mapper      数据访问层（6 个）
                                                  ├─ Entity      实体层（6 个 Java Bean）
                                                  └─ Handler     全局异常处理

                                                     │  JDBC
                                                     ▼
                                                 MySQL 数据库 (tutoring_center)
                                                  ├─ teachers            教师信息表（含 password）
                                                  ├─ subjects            科目基础表
                                                  ├─ classes             班级排课表
                                                  ├─ students            学生档案表（含 password）
                                                  ├─ student_enrollments 选课报名桥接表
                                                  └─ accounts            账目流水表
```

### 3.2 后端分层架构

采用标准 MVC 四层架构：

| 层级 | 包路径 | 职责 |
|------|--------|------|
| **Entity** | `entity/` | 数据库表对应的 Java 实体类，纯数据载体 |
| **Mapper** | `mapper/` | 数据访问层，使用 MyBatis 注解执行 SQL |
| **Service** | `service/` | 业务逻辑层，核心算法、事务管理、数据校验 |
| **Controller** | `controller/` | 控制层，对外暴露 REST API，处理 HTTP 请求 |

### 3.3 前端分层架构

| 层级 | 路径 | 职责 |
|------|------|------|
| **Router** | `router/` | 路由配置，URL ↔ 页面组件映射 |
| **Layout** | `layouts/` | 全局布局（侧边栏 + 顶栏 + 内容区） |
| **Views** | `views/` | 6 个页面组件，对应各业务模块 |
| **API** | `api/` | 封装 Axios 请求，一个模块一个文件 |
| **Utils** | `utils/` | 全局工具（Axios 实例、拦截器） |

---

## 四、项目结构

```
DBACourseProject
│
├─ database/                          # 数据库脚本
│   ├─ schema.sql                     #   主表结构（6 张实体表，对应 E-R 图）
│   ├─ features.sql                   #   功能表（teacher_subjects 桥接 + notifications 通知）
│   └─ seed_data.sql                  #   种子数据（8 教师 + 8 科目 + 17 班级 + 15 学生）
│
├─ backend/                           # 后端 Spring Boot 项目
│   └─ center-management/
│       ├─ pom.xml                    #   Maven 依赖配置
│       └─ src/main/
│           ├─ resources/
│           │   └─ application.properties   # 数据库连接、MyBatis 配置
│           └─ java/com/training/centermanagement/
│               ├─ CenterManagementApplication.java   # 启动类
│               ├─ config/
│               │   └─ WebConfig.java                # CORS 跨域配置
│               ├─ dto/
│               │   └─ Result.java                   # 统一响应体（code + message + data）
│               ├─ handler/
│               │   └─ GlobalExceptionHandler.java   # 全局异常处理（加分项）
│               ├─ entity/               # 实体层（6 个）
│               │   ├─ Teacher.java
│               │   ├─ Subject.java
│               │   ├─ Classes.java
│               │   ├─ Student.java
│               │   ├─ StudentEnrollment.java
│               │   └─ Account.java
│               ├─ mapper/               # 数据访问层（6 个）
│               │   ├─ TeacherMapper.java
│               │   ├─ SubjectMapper.java
│               │   ├─ ClassesMapper.java
│               │   ├─ StudentMapper.java
│               │   ├─ StudentEnrollmentMapper.java
│               │   └─ AccountMapper.java
│               ├─ service/              # 业务逻辑层（8 个）
│               │   ├─ TeacherService.java
│               │   ├─ SubjectService.java
│               │   ├─ ClassesService.java
│               │   ├─ StudentService.java
│               │   ├─ StudentEnrollmentService.java
│               │   ├─ AccountService.java
│               │   ├─ ScheduleService.java
│               │   └─ AuthService.java              # 统一登录认证 + 改密
│               └─ controller/           # 控制层（8 个）
│                   ├─ TeacherController.java
│                   ├─ SubjectController.java
│                   ├─ ClassesController.java
│                   ├─ StudentController.java
│                   ├─ StudentEnrollmentController.java
│                   ├─ AccountController.java
│                   ├─ ScheduleController.java
│                   └─ AuthController.java           # 登录 + 改密接口
│
├─ frontend/                           # 前端 Vue 3 项目
│   ├─ package.json                    #   依赖配置
│   ├─ vite.config.js                  #   Vite 构建配置
│   ├─ index.html                      #   入口 HTML
│   └─ src/
│       ├─ main.js                     #   应用入口
│       ├─ App.vue                     #   根组件
│       ├─ style.css                   #   全局样式
│       ├─ router/
│       │   └─ index.js                #   路由配置（8 个页面路由 + 角色守卫）
│       ├─ layouts/
│       │   └─ MainLayout.vue          #   主布局（侧边栏 + 顶栏 + 路由出口）
│       ├─ utils/
│       │   └─ request.js              #   Axios 全局封装
│       ├─ stores/
│       │   └─ auth.js                 #   Pinia 认证状态（角色、用户信息持久化）
│       ├─ components/
│       │   └─ SidebarNav.vue          #   侧边栏导航（角色过滤 + 用户信息 + 改密入口）
│       ├─ api/                        #   API 接口层（8 个模块）
│       │   ├─ teacher.js
│       │   ├─ subject.js
│       │   ├─ classes.js
│       │   ├─ student.js
│       │   ├─ enrollment.js
│       │   ├─ account.js
│       │   ├─ schedule.js
│       │   └─ auth.js                 #   登录 + 改密码
│       └─ views/                      #   页面组件（9 个）
│           ├─ LoginView.vue           #     统一登录页
│           ├─ StudentEnrollment.vue   #     学生报名页
│           ├─ MyCourses.vue           #     已选课程页
│           ├─ SubjectManage.vue       #     科目管理页
│           ├─ ClassManage.vue         #     班级管理页
│           ├─ TeacherManage.vue       #     教师管理页
│           ├─ FeeManage.vue           #     收费管理页
│           ├─ ScheduleQuery.vue       #     课表查询页
│           └─ StudentManage.vue       #     学生管理页
│
├─ log/                                # 开发日志
│   ├─ day01.md                        #   Day01：项目规划与环境搭建
│   ├─ day02.md                        #   Day02：数据库设计与 Spring Boot 搭建
│   ├─ day03.md                        #   Day03：后端核心开发与 API 调试
│   ├─ day04.md                        #   Day04：后端完善与前端全面开发
│   ├─ day05.md                        #   Day05：Bug 修复与功能完善
│   ├─ day06.md                        #   Day06：假登录 + 前端全面优化
│   ├─ day07.md                        #   Day07：登录认证系统 + 教师薪酬 + 统一登录
│   └─ Images/                         #   日志截图
│
└─ README.md                           # 项目说明文档（本文件）
```

---

## 五、数据库设计

### 5.1 E-R 模型

系统包含 6 个核心实体，实体间关系如下：

![ER](./log/Images/day02图1.png)

- **students** 与 **classes** 为多对多关系，通过 **student_enrollments** 桥接表分解为两个一对多
- **classes** 是核心枢纽：关联 subjects（科目）、teachers（教师），并承载排课、定价、名额等核心属性
- **accounts** 记录每笔独立缴费流水，与 student_enrollments 形成对账稽核闭环

### 5.2 表结构详情

#### 5.2.1 教师表 (teachers)

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| teacher_id | INT | PRIMARY KEY | 教师工号 |
| teacher_name | VARCHAR(50) | NOT NULL | 教师姓名 |
| teacher_level | VARCHAR(20) | — | 教师等级（金牌教师/高级教师/特级教师/中级教师） |
| specialty | VARCHAR(100) | — | 特长科目描述（如：奥数,围棋） |
| password | VARCHAR(100) | NOT NULL DEFAULT '111111' | 登录密码 |

#### 5.2.2 科目表 (subjects)

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| subject_id | INT | PRIMARY KEY | 科目编号 |
| subject_name | VARCHAR(50) | NOT NULL | 科目名称（如：奥数、围棋、书法、口才） |
| hours | INT | NOT NULL | 标准总学时 |

#### 5.2.3 班级排课表 (classes)

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| class_code | VARCHAR(20) | PRIMARY KEY | 班级代号（如：MATH-2026-01） |
| subject_id | INT | FOREIGN KEY → subjects | 所属科目 |
| teacher_id | INT | FOREIGN KEY → teachers | 任课教师 |
| term | VARCHAR(50) | NOT NULL | 开班期次（如：2026春季班） |
| period | VARCHAR(50) | — | 上课时间（如：每周六 09:00-11:00） |
| fee | DECIMAL(10,2) | NOT NULL | 该班学费（因教师等级而异） |
| location | VARCHAR(100) | — | 教室（如：A栋101） |
| capacity | INT | NOT NULL | 招收人数上限 |
| enrolled_count | INT | DEFAULT 0 | 当前已报名人数 |
| teacher_remuneration | DECIMAL(10,2) | NOT NULL | 教师课时报酬 |

**enrolled_count <= capacity** 通过 CHECK 约束在数据库底层保障。

#### 5.2.4 学生表 (students)

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| student_id | INT | PRIMARY KEY | 学生编号 |
| student_name | VARCHAR(50) | NOT NULL | 学生姓名 |
| registration_time | DATETIME | NOT NULL | 建档注册时间 |
| password | VARCHAR(100) | NOT NULL DEFAULT '111111' | 登录密码 |

#### 5.2.5 学生选课报名表 (student_enrollments)

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| student_id | INT | PRIMARY KEY (复合), FK → students | 学生编号 |
| class_code | VARCHAR(20) | PRIMARY KEY (复合), FK → classes | 班级代号 |
| enrollment_time | DATETIME | NOT NULL | 报名时间 |
| amount_paid | DECIMAL(10,2) | DEFAULT 0.00 | 累计已缴金额 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'active' | 报名状态（active-在读 / cancelled-已退课） |

复合主键 **(student_id, class_code)** 天然防止同一学生重复报名同一班级。退课仅软删除（标记 cancelled），流水保留不变，全额退款通过负向冲销实现。同科目重复报名在后端 processEnrollment 中拦截（遍历 active 报名比对 subject_id）。

#### 5.2.6 账目流水表 (accounts)

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|----------|------|------|
| account_id | INT | PRIMARY KEY, AUTO_INCREMENT | 流水号 |
| account_date | DATE | NOT NULL | 交易日期 |
| class_code | VARCHAR(20) | FOREIGN KEY → classes | 班级代号 |
| student_id | INT | FOREIGN KEY → students | 学生编号 |
| subject_id | INT | FOREIGN KEY → subjects | 科目编号（冗余便于科目维度统计） |
| amount_paid | DECIMAL(10,2) | NOT NULL | 本次实缴金额 |

每条缴费生成一条不可逆流水记录，通过 **SUM(amount_paid)** 与 **student_enrollments.amount_paid** 进行错账稽核。

---

## 六、后端 API 文档

Base URL: http://localhost:8080/api

### 6.1 认证模块 — `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 统一登录（自动识别学生/教师/管理员） |
| PUT | `/api/auth/change-password` | 修改密码（学生/教师） |

### 6.2 教师模块 — `/api/teachers`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/teachers` | 获取全部教师列表 |
| GET | `/api/teachers/salaries` | 获取全部教师薪酬汇总 |
| GET | `/api/teachers/by-subject/{subjectId}` | 按任教科目精确查询教师 |
| GET | `/api/teachers/{teacherId}` | 按 ID 查询教师 |

### 6.3 科目模块 — `/api/subjects`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/subjects` | 获取全部科目 |
| GET | `/api/subjects/{subjectId}` | 按 ID 查询科目 |
| POST | `/api/subjects` | 新增科目（含 ID 重复校验） |
| PUT | `/api/subjects` | 更新科目 |
| DELETE | `/api/subjects/{subjectId}` | 删除科目（有关联班级时阻止） |

### 6.4 班级模块 — `/api/classes`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/classes` | 获取全部班级 |
| GET | `/api/classes/{classCode}` | 按代号查询班级 |
| GET | `/api/classes/by-subject?subjectId=` | 按科目 ID 查询班级列表 |
| POST | `/api/classes` | 新增班级（含外键校验与代号重复校验） |

### 6.5 学生模块 — `/api/students`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/students` | 获取全部学生 |
| GET | `/api/students/{studentId}` | 按 ID 查询学生 |
| POST | `/api/students` | 新增学生（含 ID 重复校验） |
| PUT | `/api/students` | 更新学生 |
| DELETE | `/api/students/{studentId}` | 删除学生（全 cancelled 后允许物理清理） |

### 6.6 报名模块 — `/api/enrollments`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/enrollments/submit` | 提交报名（核心事务接口） |
| DELETE | `/api/enrollments/cancel?studentId=&classCode=` | 退课（退回已缴金额 + 恢复班级名额） |
| GET | `/api/enrollments/check?studentId=&classCode=` | 检查报名状态（含历史取消记录） |

**报名流程**（`StudentEnrollmentService.processEnrollment`，六重门禁）：

1. 校验学生是否存在
2. 校验是否重复报名同一班级
3. 检测 cancelled 记录 → UPDATE 复用（重新报名）
4. 校验班级是否存在
5. **同科目防重复**：遍历 active 报名比对 subject_id
6. 检查是否满员（`enrolled_count >= capacity`）
7. **学生总维度缴费校验**：已缴 + 本次 <= 所有 active 课程学费之和
8. 原子性递增 `enrolled_count`（`WHERE enrolled_count < capacity` 防超卖）
9. 写入 `student_enrollments` + `accounts`，同一事务中

### 6.7 账目模块 — `/api/accounts`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/accounts` | 查询全部流水记录 |
| POST | `/api/accounts/pay` | 单独缴费/补缴（校验报名 + 写入流水 + 更新已缴金额） |
| GET | `/api/accounts/invoice?studentId=&classCode=` | 打印收费清单（应缴/已缴/欠费 + 缴费明细） |
| GET | `/api/accounts/debtors` | 查询欠费学生列表 |
| GET | `/api/accounts/student-summary?studentId=` | 查询学生缴费汇总（报名列表 + 缴费明细 + 欠费统计） |

### 6.8 课表模块 — `/api/schedules`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/schedules/student/{studentId}` | 学生课表（该生所有班级的上课时间与教室） |
| GET | `/api/schedules/teacher/{teacherId}` | 教师课表（含学生人数、课时报酬） |

---

## 七、前端页面说明

### 7.1 登录认证

系统使用统一的登录入口，学生、教师和管理员共用同一个登录表单。输入学号/工号/管理员名 + 密码后，后端自动识别身份并跳转到对应首页。

- **路由守卫**：`beforeEach` 根据角色权限拦截未授权页面访问
- **状态持久化**：Pinia store + localStorage，刷新页面不丢失登录状态
- **密码修改**：学生和教师可在侧边栏修改密码，管理员密码固定

### 7.2 角色菜单

| 角色 | 可见菜单 |
|------|----------|
| 学生 | 学生报名、已选课程、课表查询 |
| 教师 | 课表查询（含薪酬汇总） |
| 管理员 | 全部 7 个菜单（学生报名、科目管理、班级管理、教师管理、学生管理、收费管理、课表查询） |

### 7.3 页面功能详情

| 页面 | 路由 | 角色 | 核心功能 |
|------|------|------|----------|
| **登录页** | `/login` | 全部 | 统一登录表单（学号/工号/管理员名 + 密码），后端自动识别身份 |
| **学生报名** | `/enrollment` | 学生/管理员 | 学生端：选科目->选班级->选填缴费->提交；管理端：先输学号->显示学生信息->无缴费金额报名表单->已选课程列表+退课 |
| **已选课程** | `/my-courses` | 学生 | 课程列表（科目/老师/级别/学费/已缴/状态）+ 补缴弹窗 + 退课 + 汇总卡片 + 催费提示 |
| **科目管理** | `/subjects` | 管理员 | 科目 CRUD（表格 + 弹窗表单） |
| **班级管理** | `/classes` | 管理员 | 班级列表（选科后按任教科目精确过滤教师、enrolledCount 编辑时禁用、满员标红） |
| **教师管理** | `/teachers` | 管理员 | 教师 CRUD（等级下拉选择，删除前 FK 检查） |
| **收费管理** | `/fee` | 管理员 | 四标签页：缴费查询（含退课）+ 流水记录 + 催费列表 + 教师薪酬 |
| **课表查询** | `/schedule` | 全部 | 学生/教师：自动加载个人课表；管理员：输入学号/工号->自动识别类型->对应课表+人员信息 |
| **学生管理** | `/students` | 管理员 | 学生 CRUD（全 cancelled 后允许删除） |

---

## 八、运行说明

### 8.1 环境要求

| 软件 | 最低版本 |
|------|----------|
| JDK | 17+ |
| Maven | 3.6+ |
| Node.js | 18+ |
| npm | 9+ |
| MySQL | 8.0+ |

### 8.2 数据库初始化

1. 启动 MySQL 服务
2. 使用 MySQL Workbench 或命令行执行：

```sql
source database/schema.sql;
source database/features.sql;
source database/seed_data.sql;
```

### 8.3 后端启动

```bash
cd backend/center-management

# 首次运行需安装依赖
./mvnw install

# 启动 Spring Boot 应用
./mvnw spring-boot:run
```

后端启动后访问地址：`http://localhost:8080`

验证接口示例：`http://localhost:8080/api/teachers`

### 8.4 前端启动

```bash
cd frontend

# 首次运行需安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问地址：`http://localhost:5173`

### 8.5 配置说明

**数据库连接** — `backend/center-management/src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tutoring_center
spring.datasource.username=<你的MySQL用户名>
spring.datasource.password=<你的MySQL密码>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
mybatis.configuration.map-underscore-to-camel-case=true
```

**前端 API 地址** — `frontend/src/utils/request.js`：

```js
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000,
})
```

---

## 九、需求完成度对照

| 课程设计要求 | 实现情况 | 对应模块 |
|-------------|----------|----------|
| 学生报名（满员检查→选教师→报名→清单） | ✅ 完整实现 | `StudentEnrollmentService.processEnrollment` |
| 科目维护（增删改查） | ✅ 完整实现 | `SubjectController` CRUD |
| 同一科目不同教师差异化收费 | ✅ 在 classes 表中定价 | `classes.fee` + `classes.teacher_remuneration` |
| 满员提示报名下期 | ✅ 满员拦截 + term 字段 | 报名返回"已满员"提示 |
| 安排教室及上课日程 | ✅ 完整实现 | `ScheduleController` 学生/教师课表 |
| 账目管理（入账/收据/清单/催费） | ✅ 完整实现 | `AccountController` 四功能 |
| 数据要求：学生信息 | ✅ | `students` + `student_enrollments` |
| 数据要求：科目信息 | ✅ | `subjects` + `classes` 联合承载 |
| 数据要求：教师信息 | ✅ | `teachers` |
| 数据要求：账目信息 | ✅ | `accounts` |
| 角色登录认证 | ✅ 完整实现 | `AuthService.loginAuto` 三种身份自动识别 |
| 学生补缴欠费 | ✅ 完整实现 | 缴费状态卡片 + arrears 计算 + 补缴弹窗 |
| 教师薪酬统计 | ✅ 完整实现 | 教师端汇总卡 + 管理端教师薪酬标签页 |
| 退课自动退款 | ✅ 完整实现 | 负向冲销，全额退还 |
| 同科目防重复报名 | ✅ 完整实现 | processEnrollment 第 4.5 步 subject_id 遍历 |
| 管理员两阶段交互 | ✅ 完整实现 | 查询后卡片消失，展示全新内容 |
| 数据库密码加密 | ✅ 完整实现 | Jasypt PBEWITHHMACSHA512ANDAES_256 |
| 事务管理（加分项） | ✅ | `@Transactional` 报名 + 缴费 + 退课 |
| 全局异常处理（加分项） | ✅ | `GlobalExceptionHandler` |
| 敏感数据加解密（加分项） | ✅ | Jasypt 加密 application.properties 密码 |

---

## 十、开发日志

| 日期 | 文档 | 主要内容 |
|------|------|----------|
| 2026.6.1 | [Day01](log/day01.md) | 项目规划、Git 仓库创建、技术选型、目录结构搭建 |
| 2026.6.3～6.4 | [Day02](log/day02.md) | ER 图设计、6 张表建表、范式优化讨论、Spring Boot 项目创建、第一个 API 测试 |
| 2026.6.5 | [Day03](log/day03.md) | 后端核心开发、报名事务流程、跨域配置、防重复校验、全部接口调试 |
| 2026.6.6 | [Day04](log/day04.md) | 后端完善（科目 CRUD + 课表 + 缴费/清单/催费）、前端全面开发（6 页面） |
| 2026.6.9 | [Day05](log/day05.md) | Claude Code 全量代码审查，修复 11 个问题：删除 FK 检查、enrolledCount 防篡改、假删除成功语义检测、Entity Lombok 化、全局异常处理 |
| 2026.6.10 | [Day06](log/day06.md) | 退款清算系统：status 软删除、退课全额退款（负向冲销）、学生总维度缴费规则、教师特长过滤、退课后重新报名（UPDATE 复用） |
| 2026.6.11 | [Day07](log/day07.md) | 登录认证系统（AuthService + Pinia + 路由守卫）、角色权限、学生补缴修复、教师薪酬（双端）、课表角色隔离 |
| 2026.6.12 | [Day08](log/day08.md) | 前端结构重构（MyCourses + 管理端两阶段交互 + 课表统一 ID）、同科目防重复、后端接口增强、Jasypt 加密、全流程联调、课设报告 |

---

## 十一、GitHub 仓库

🔗 [https://github.com/Tru0Gruanol/DBACourseProject](https://github.com/Tru0Gruanol/DBACourseProject)

---

*本项目为数据库课程设计项目，持续开发中。*

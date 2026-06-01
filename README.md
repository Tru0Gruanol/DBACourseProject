\# DBA Course Project



\## 1. 项目简介



本项目为数据库课程设计系统，采用前后端分离架构，实现基础的数据管理与业务操作流程。项目包含数据库设计、后端服务开发以及前端页面展示，并使用 Git 进行版本管理与开发过程记录。



项目目标是完成一个完整的可运行系统，并记录每日开发过程。



\---



\## 2. 技术栈



\### 后端



\* Spring Boot

\* MyBatis

\* Java



\### 前端



\* Vue.js

\* HTML / CSS / JavaScript



\### 数据库



\* MySQL



\### 工具



\* Git / GitHub

\* Maven



\---



\## 3. 项目结构



```

DBACourseProject

│

├─ database        # 数据库脚本（建表SQL、初始化数据）

├─ backend         # 后端Spring Boot项目

├─ frontend        # 前端Vue项目

├─ log             # 每日开发日志（Markdown）

└─ README.md       # 项目说明文档

```



\---



\## 4. 功能说明（计划）



\* 用户管理（增删改查）

\* 数据查询与展示

\* 基础业务流程处理

\* 前后端接口通信

\* 数据库持久化存储



\---



\## 5. 开发日志规范



每一天开发内容记录在 `log` 文件夹中，例如：



\* day01.md：项目初始化与环境搭建

\* day02.md：数据库设计

\* day03.md：后端接口开发

\* day04.md：前端页面开发



通过 Git 提交记录配合日志文件跟踪开发过程。



\---



\## 6. 运行说明（后续补充）



\### 后端启动



```

cd backend

运行 Spring Boot 项目

```



\### 前端启动



```

cd frontend

npm install

npm run dev

```



\### 数据库



执行 `database` 文件夹中的 SQL 脚本初始化数据库。



\---



\## 7. 版本管理



本项目使用 Git 进行版本控制，每日提交开发进度：



```

git add .

git commit -m "DayXX: xxx"

git push

```



\---



\## 8. 说明



本项目为课程设计项目，持续开发中。




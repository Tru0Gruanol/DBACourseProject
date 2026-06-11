-- ============================================================
-- Day08 数据库迁移：为 students 和 teachers 表添加密码字段
-- 用途：支持学生端和教师端的身份验证登录
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 < migrate_day08.sql
-- ============================================================

USE tutoring_center;

-- 学生表添加密码字段（默认密码 111111）
ALTER TABLE students
ADD COLUMN password VARCHAR(100) NOT NULL DEFAULT '111111'
COMMENT '登录密码';

-- 教师表添加密码字段（默认密码 111111）
ALTER TABLE teachers
ADD COLUMN password VARCHAR(100) NOT NULL DEFAULT '111111'
COMMENT '登录密码';

-- 验证
DESC students;
DESC teachers;

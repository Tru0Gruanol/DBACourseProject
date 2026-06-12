-- ============================================================
-- 主表结构（6 张实体表，对应 E-R 图）
-- 执行：mysql -u root -p --default-character-set=utf8mb4 < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS tutoring_center CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tutoring_center;

-- 按外键依赖顺序删表
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS student_enrollments;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS subjects;
DROP TABLE IF EXISTS teachers;

-- 1. 教师信息表
CREATE TABLE teachers (
    teacher_id   INT NOT NULL PRIMARY KEY COMMENT '教师工号',
    teacher_name VARCHAR(50) NOT NULL COMMENT '教师姓名',
    teacher_level VARCHAR(20) DEFAULT NULL COMMENT '教师等级',
    specialty    VARCHAR(100) DEFAULT NULL COMMENT '特长描述',
    password     VARCHAR(100) NOT NULL DEFAULT '111111' COMMENT '登录密码'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

-- 2. 科目基础信息表
CREATE TABLE subjects (
    subject_id   INT NOT NULL PRIMARY KEY COMMENT '科目编号',
    subject_name VARCHAR(50) NOT NULL COMMENT '科目名称',
    hours        INT NOT NULL COMMENT '标准总学时'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目基础信息表';

-- 3. 班级排课信息表（核心枢纽）
CREATE TABLE classes (
    class_code           VARCHAR(20) NOT NULL PRIMARY KEY COMMENT '班级代号',
    subject_id           INT NOT NULL COMMENT '所属科目',
    teacher_id           INT NOT NULL COMMENT '任课教师',
    term                 VARCHAR(50) NOT NULL COMMENT '开班期次',
    period               VARCHAR(50) DEFAULT NULL COMMENT '上课时间',
    fee                  DECIMAL(10,2) NOT NULL COMMENT '学费',
    location             VARCHAR(100) DEFAULT NULL COMMENT '教室',
    capacity             INT NOT NULL COMMENT '招收人数上限',
    enrolled_count       INT DEFAULT 0 COMMENT '已报名人数',
    teacher_remuneration DECIMAL(10,2) NOT NULL COMMENT '教师课时报酬',
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),
    CONSTRAINT chk_capacity CHECK (enrolled_count <= capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级排课信息表';

-- 4. 学生基础信息表
CREATE TABLE students (
    student_id        INT NOT NULL PRIMARY KEY COMMENT '学生编号',
    student_name      VARCHAR(50) NOT NULL COMMENT '学生姓名',
    registration_time DATETIME NOT NULL COMMENT '建档注册时间',
    password          VARCHAR(100) NOT NULL DEFAULT '111111' COMMENT '登录密码'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生基础信息表';

-- 5. 选课报名桥接表（students ↔ classes 多对多）
CREATE TABLE student_enrollments (
    student_id      INT NOT NULL COMMENT '学生编号',
    class_code      VARCHAR(20) NOT NULL COMMENT '班级代号',
    enrollment_time DATETIME NOT NULL COMMENT '报名时间',
    amount_paid     DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计已缴金额',
    status          VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT 'active-在读 / pending_cancel-待审批 / cancelled-已退课',
    PRIMARY KEY (student_id, class_code),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (class_code) REFERENCES classes(class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课报名关联表';

-- 6. 账目明细流水表
CREATE TABLE accounts (
    account_id   INT AUTO_INCREMENT NOT NULL PRIMARY KEY COMMENT '流水号',
    account_date DATE NOT NULL COMMENT '交易日期',
    class_code   VARCHAR(20) NOT NULL COMMENT '班级代号',
    student_id   INT NOT NULL COMMENT '学生编号',
    subject_id   INT NOT NULL COMMENT '科目编号（冗余便于科目维度统计）',
    amount_paid  DECIMAL(10,2) NOT NULL COMMENT '本次实缴金额（退款为负）',
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (class_code) REFERENCES classes(class_code),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账目流水表';

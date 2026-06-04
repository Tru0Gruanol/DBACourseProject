create database if not exists tutoring_center;
use tutoring_center;

-- 1. 教师信息表 (对应数据要求：教师号、教师名、教师等级、教师特长)

CREATE TABLE teachers (
    teacher_id INT NOT NULL COMMENT '教师工号，唯一主键',
    teacher_name VARCHAR(50) NOT NULL COMMENT '教师真实姓名',
    teacher_level VARCHAR(20) DEFAULT NULL COMMENT '教师等级(如：金牌教师、高级教师)，后台据此计算课时报酬及学员学费',
    specialty VARCHAR(100) DEFAULT NULL COMMENT '教师特长科目描述(如：奥数、围棋、吉他等特长)',
    PRIMARY KEY (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师基本信息及等级资质表';



-- 2. 科目基础信息表 (对应数据要求：科目号、科目名、学时)
-- 注：此处仅存放科目的全局静态基础属性，具体开班、排课、动态学费则在班级表中维护

CREATE TABLE subjects (
    subject_id INT NOT NULL COMMENT '基础科目号，唯一主键',
    subject_name VARCHAR(50) NOT NULL COMMENT '科目名称(如：奥数、围棋、口才、新概念)',
    hours INT NOT NULL COMMENT '该科目单期培训的标准总学时数',
    PRIMARY KEY (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科目基础静态数据表';



-- 3. 班级排课信息表 (合并对应数据要求：上课周期、收费、上课地点、教师号、招收人数、已报名人数)
-- 业务闭环：通过引入 term (期次) 字段，完美支持“当前班级满员后，系统引导报名下期”的硬性核心要求

CREATE TABLE classes (
    class_code VARCHAR(20) NOT NULL COMMENT '班级代号/排课编号，唯一主键(账目表的核心关联纽带)',
    subject_id INT NOT NULL COMMENT '关联的基础科目号',
    teacher_id INT NOT NULL COMMENT '关联的任课教师号(同一科目可由知名度不同的教师承担，生成不同班级)',
    term VARCHAR(50) NOT NULL COMMENT '开班期次(如：2026春季班、2026暑假一期)，用于满员时系统关联并引导至下一期',
    period VARCHAR(50) DEFAULT NULL COMMENT '上课周期/具体上课时间段描述(如：每周六 14:00-16:00)',
    fee DECIMAL(10, 2) NOT NULL COMMENT '该班级的收费标准/应缴学费(由科目类型与任课教师知名度共同决定)',
    location VARCHAR(100) DEFAULT NULL COMMENT '上课地点/安排的教室(如：302多媒体教室)',
    capacity INT NOT NULL COMMENT '该班级的招收人数上限(最大容量)',
    enrolled_count INT DEFAULT 0 COMMENT '当前该班级已成功报名登记的学员总人数，由选课事务触发更新',
    teacher_remuneration DECIMAL(10, 2) NOT NULL COMMENT '给该任课老师发放的课时报酬(根据教师等级/知名度定制化设定)',
    PRIMARY KEY (class_code),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),
    CONSTRAINT chk_capacity CHECK (enrolled_count <= capacity) -- 数据库底层约束：严禁已报人数超过招收人数上限
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级排课与上课日程主表';


-- 4. 学生基础信息表 (对应数据要求：学生编号、学生姓名、报名时间)
-- 注：此处的 registration_time 仅代表该学生首次来到培训中心建档、办卡开户的系统总时间

CREATE TABLE students (
    student_id INT NOT NULL COMMENT '学生编号/学号，唯一主键',
    student_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    registration_time DATETIME NOT NULL COMMENT '学生首次在培训中心开户、建立档案的注册时间',
    PRIMARY KEY (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生基础档案信息表';



-- 5. 学生选课报名关联表 (多对多桥接表，满足要求：学生所选科目可能不止一门、交款额)
-- 核心优化：引入单科 enrollment_time 避免时间混乱；amount_paid 明确定义为“该学生在此班级累计已交总额的缓存”

CREATE TABLE student_enrollments (
    student_id INT NOT NULL COMMENT '复合主键，关联学生编号',
    class_code VARCHAR(20) NOT NULL COMMENT '复合主键，关联具体的班级代号',
    enrollment_time DATETIME NOT NULL COMMENT '该学生报名选修这门课/该班级的具体登记时间',
    amount_paid DECIMAL(10, 2) DEFAULT 0.00 COMMENT '该生针对该班级【目前已缴纳的学费总额】(用于与classes.fee对比，快速催缴欠费)',
    PRIMARY KEY (student_id, class_code),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (class_code) REFERENCES classes(class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生单科选课报名与缴费总额汇总表';



-- 6. 账目明细流水表 (对应数据要求：日期、班级代号、学生编号、科目号、交款额)
-- 财务闭环：作为开具收据、打印清单的底层数据，只记录单次实际交款。通过 SUM(流水) 可与桥接表进行错账稽核。

CREATE TABLE accounts (
    account_id INT AUTO_INCREMENT NOT NULL COMMENT '财务账目流水号，自增主键',
    account_date DATE NOT NULL COMMENT '单笔交易实际发生/开具收据清单的财务日期',
    class_code VARCHAR(20) NOT NULL COMMENT '交款所指向的特定班级代号',
    student_id INT NOT NULL COMMENT '交款的学生编号',
    subject_id INT NOT NULL COMMENT '交款关联的科目号(直接读取，方便不联表的情况下进行科目维度的财务账目统计)',
    amount_paid DECIMAL(10, 2) NOT NULL COMMENT '【单次实际交款额】(支持学生开学时分期、多次补缴，每次缴费生成一条流水记录)',
    PRIMARY KEY (account_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (class_code) REFERENCES classes(class_code),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账目明细流水与收费清单表';
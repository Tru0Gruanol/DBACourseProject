-- ============================================================
-- 功能表（E-R 图之外的辅助表）
-- teacher_subjects：教师-科目 M:N 桥接（从 specialty 文本规范化而来）
-- notifications：退课审批通知 + 铃铛未读提醒
-- 执行：mysql -u root -p --default-character-set=utf8mb4 < features.sql
-- ============================================================

USE tutoring_center;

DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS teacher_subjects;

-- 教师任教科目桥接表（teachers ↔ subjects 多对多）
CREATE TABLE teacher_subjects (
    teacher_id INT NOT NULL COMMENT '教师工号',
    subject_id INT NOT NULL COMMENT '科目编号',
    PRIMARY KEY (teacher_id, subject_id),
    FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师任教科目关联表';

-- 通知消息表（退课审批、退款通知）
CREATE TABLE notifications (
    id         INT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    user_id    INT NOT NULL COMMENT '接收者ID',
    user_role  VARCHAR(10) NOT NULL COMMENT '接收者角色(student/teacher/admin)',
    title      VARCHAR(200) NOT NULL COMMENT '通知标题',
    content    VARCHAR(500) DEFAULT '' COMMENT '通知内容',
    is_read    TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';

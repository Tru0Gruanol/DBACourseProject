-- Day07 数据库迁移：student_enrollments 表增加 status 字段
-- 退课不再删除记录，而是标记为 'cancelled'

ALTER TABLE student_enrollments
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active'
COMMENT '报名状态：active-在读，cancelled-已退课';

-- 索引优化（可选）
CREATE INDEX idx_enrollment_status ON student_enrollments(status);

package com.training.centermanagement.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 启动时自动执行数据库迁移（建表幂等，避免手动执行 SQL 文件）
 */
@Component
@org.springframework.context.annotation.Profile("!reset")
public class DatabaseMigration implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. 创建 teacher_subjects 桥接表（IF NOT EXISTS 保证幂等）
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS teacher_subjects (" +
                "  teacher_id INT NOT NULL COMMENT '教师工号'," +
                "  subject_id INT NOT NULL COMMENT '科目编号'," +
                "  PRIMARY KEY (teacher_id, subject_id)," +
                "  FOREIGN KEY (teacher_id) REFERENCES teachers(teacher_id)," +
                "  FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师任教科目关联表'"
            );

            // 2. 从现有 specialty 字段迁移数据（INSERT IGNORE 保证幂等）
            stmt.execute(
                "INSERT IGNORE INTO teacher_subjects (teacher_id, subject_id) " +
                "SELECT t.teacher_id, s.subject_id FROM teachers t " +
                "JOIN subjects s ON t.specialty LIKE CONCAT('%', s.subject_name, '%')"
            );

            // 3. notifications 通知表
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS notifications (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  user_role VARCHAR(10) NOT NULL," +
                "  title VARCHAR(200) NOT NULL," +
                "  content VARCHAR(500) DEFAULT ''," +
                "  is_read TINYINT NOT NULL DEFAULT 0," +
                "  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表'"
            );

            System.out.println("[DatabaseMigration] 所有迁移完成");
        }
    }
}

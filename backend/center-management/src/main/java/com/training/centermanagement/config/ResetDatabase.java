package com.training.centermanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 一次性数据库重置工具。
 * 启动: ./mvnw spring-boot:run -Dspring-boot.run.profiles=reset
 * 完毕后 Ctrl+C 停掉，去掉 reset profile 重启即可。
 */
@Component
@Profile("reset")
public class ResetDatabase implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ResetDatabase.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("======== 开始重置数据库 ========");

        try (Connection conn = dataSource.getConnection()) {
            // 1. 删除所有业务表（按外键依赖顺序）
            String[] drops = {
                "DROP TABLE IF EXISTS accounts",
                "DROP TABLE IF EXISTS student_enrollments",
                "DROP TABLE IF EXISTS teacher_subjects",
                "DROP TABLE IF EXISTS students",
                "DROP TABLE IF EXISTS classes",
                "DROP TABLE IF EXISTS subjects",
                "DROP TABLE IF EXISTS teachers",
            };
            for (String dropSql : drops) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(dropSql);
                    log.info("  已删除: {}", dropSql.substring(18));
                }
            }

            // 2. 按顺序执行三个脚本
            String base = "../../database/";
            log.info("  执行 schema.sql ...");
            executeSimpleSql(conn, base + "schema.sql");
            log.info("  执行 features.sql ...");
            executeSimpleSql(conn, base + "features.sql");
            log.info("  执行 seed_data.sql ...");
            executeSimpleSql(conn, base + "seed_data.sql");

            log.info("======== 数据库重置完成，请去掉 -Dspring-boot.run.profiles=reset 重新启动 ========");
        }
    }

    /** 执行纯 DML/DDL 的 SQL 文件（不含存储过程） */
    private void executeSimpleSql(Connection conn, String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                sb.append(line).append("\n");
            }
        }
        String[] statements = sb.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String rawSql : statements) {
                String trimmedSql = rawSql.trim();
                if (trimmedSql.isEmpty()) continue;
                if (trimmedSql.toUpperCase().contains("DELIMITER")
                    || trimmedSql.toUpperCase().startsWith("CREATE PROCEDURE")
                    || trimmedSql.toUpperCase().startsWith("CREATE FUNCTION")) {
                    continue;
                }
                try {
                    stmt.execute(trimmedSql);
                } catch (Exception ex) {
                    String detail = ex.getMessage();
                    log.warn("    [跳过] {}", detail.length() > 60 ? detail.substring(0, 60) : detail);
                }
            }
        }
    }
}

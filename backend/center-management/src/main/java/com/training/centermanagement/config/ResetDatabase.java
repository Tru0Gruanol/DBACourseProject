package com.training.centermanagement.config;

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

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("========================================");
        System.out.println("[ResetDatabase] 开始重置数据库...");
        System.out.println("========================================");

        try (Connection conn = dataSource.getConnection()) {
            // 1. 删除所有业务表
            String[] drops = {
                "DROP TABLE IF EXISTS accounts",
                "DROP TABLE IF EXISTS student_enrollments",
                "DROP TABLE IF EXISTS teacher_subjects",
                "DROP TABLE IF EXISTS students",
                "DROP TABLE IF EXISTS classes",
                "DROP TABLE IF EXISTS subjects",
                "DROP TABLE IF EXISTS teachers",
            };
            for (String sql : drops) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                    System.out.println("  已删除: " + sql.substring(18));
                }
            }

            // 2. 按顺序执行三个脚本
            String base = "../../database/";
            System.out.println("  执行 schema.sql ...");
            executeSimpleSql(conn, base + "schema.sql");
            System.out.println("  执行 features.sql ...");
            executeSimpleSql(conn, base + "features.sql");
            System.out.println("  执行 seed_data.sql ...");
            executeSimpleSql(conn, base + "seed_data.sql");

            System.out.println("========================================");
            System.out.println("[ResetDatabase] 数据库重置完成！");
            System.out.println("  请停止应用，去掉 -Dspring-boot.run.profiles=reset 重新启动");
            System.out.println("========================================");
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
            for (String sql : statements) {
                String s = sql.trim();
                if (s.isEmpty()) continue;
                if (s.toUpperCase().contains("DELIMITER")
                    || s.toUpperCase().startsWith("CREATE PROCEDURE")
                    || s.toUpperCase().startsWith("CREATE FUNCTION")) {
                    continue;
                }
                try {
                    stmt.execute(s);
                } catch (Exception e) {
                    System.out.println("    [跳过] " + e.getMessage().substring(0, Math.min(60, e.getMessage().length())));
                }
            }
        }
    }
}

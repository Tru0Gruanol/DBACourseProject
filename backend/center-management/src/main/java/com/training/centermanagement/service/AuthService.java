package com.training.centermanagement.service;

import com.training.centermanagement.entity.Student;
import com.training.centermanagement.entity.Teacher;
import com.training.centermanagement.mapper.StudentMapper;
import com.training.centermanagement.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一认证服务。
 *
 * 支持三种身份登录：
 * <ul>
 *   <li>学生 — 学号（数字）→ students 表校验</li>
 *   <li>教师 — 工号（数字）→ teachers 表校验</li>
 *   <li>管理员 — 固定凭证 admin / admin123</li>
 * </ul>
 * 对外暴露单一 loginAuto 入口，前端无需角色选择。
 */
@Service
public class AuthService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    /**
     * 按指定角色登录（未被使用，保留以备后续按角色入口）。
     *
     * @param role     student / teacher
     * @param id       学号或教师号
     * @param password 密码
     * @return { success, userId, userName, message }
     */
    public Map<String, Object> login(String role, Integer id, String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        if (id == null || password == null || password.isEmpty()) {
            result.put("message", "请输入学号/教师号和密码");
            return result;
        }

        if ("student".equals(role)) {
            Student student = studentMapper.login(id, password);
            if (student != null) {
                result.put("success", true);
                result.put("userId", student.getStudentId());
                result.put("userName", student.getStudentName());
                result.put("message", "登录成功");
            } else {
                result.put("message", "登录失败：学号或密码错误");
            }
        } else if ("teacher".equals(role)) {
            Teacher teacher = teacherMapper.login(id, password);
            if (teacher != null) {
                result.put("success", true);
                result.put("userId", teacher.getTeacherId());
                result.put("userName", teacher.getTeacherName());
                result.put("message", "登录成功");
            } else {
                result.put("message", "登录失败：教师号或密码错误");
            }
        } else {
            result.put("message", "无效的角色类型");
        }
        return result;
    }

    /**
     * 自动识别角色登录（学生/教师/管理员统一入口）。
     *
     * 识别逻辑：
     * <ol>
     *   <li>纯数字 → 先查 students 表，再查 teachers 表</li>
     *   <li>非数字 + 匹配 admin/admin123 → 管理员</li>
     *   <li>以上均不匹配 → 返回失败</li>
     * </ol>
     *
     * @param username 学号、教师号或管理员名（"admin"）
     * @param password 密码
     * @return { success, userId, userName, role, message }
     */
    public Map<String, Object> loginAuto(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            result.put("message", "请输入账号和密码");
            return result;
        }

        // 尝试解析为数字 → 查学生表和教师表
        try {
            Integer id = Integer.parseInt(username);

            Student student = studentMapper.login(id, password);
            if (student != null) {
                result.put("success", true);
                result.put("role", "student");
                result.put("userId", student.getStudentId());
                result.put("userName", student.getStudentName());
                result.put("message", "登录成功");
                return result;
            }

            Teacher teacher = teacherMapper.login(id, password);
            if (teacher != null) {
                result.put("success", true);
                result.put("role", "teacher");
                result.put("userId", teacher.getTeacherId());
                result.put("userName", teacher.getTeacherName());
                result.put("message", "登录成功");
                return result;
            }
        } catch (NumberFormatException e) {
            // 非数字 → 尝试管理员
            if ("admin".equals(username) && "admin123".equals(password)) {
                result.put("success", true);
                result.put("role", "admin");
                result.put("userId", "admin");
                result.put("userName", "系统管理员");
                result.put("message", "登录成功");
                return result;
            }
        }

        result.put("message", "登录失败：账号或密码错误");
        return result;
    }

    /**
     * 修改密码（仅学生/教师，管理员密码固定）。
     *
     * @param role        student / teacher
     * @param id          学号或教师号
     * @param oldPassword 旧密码（用于验证身份）
     * @param newPassword 新密码
     * @return 操作结果提示
     */
    public String changePassword(String role, Integer id, String oldPassword, String newPassword) {
        if (id == null || oldPassword == null || oldPassword.isEmpty()) {
            return "修改失败：请输入旧密码";
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return "修改失败：新密码不能为空";
        }

        int rows;
        if ("student".equals(role)) {
            rows = studentMapper.changePassword(id, oldPassword, newPassword);
        } else if ("teacher".equals(role)) {
            rows = teacherMapper.changePassword(id, oldPassword, newPassword);
        } else {
            return "修改失败：无效的角色类型";
        }

        return rows > 0 ? "密码修改成功" : "密码修改失败：旧密码错误";
    }
}

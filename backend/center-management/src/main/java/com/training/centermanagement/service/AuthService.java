package com.training.centermanagement.service;

import com.training.centermanagement.entity.Student;
import com.training.centermanagement.entity.Teacher;
import com.training.centermanagement.mapper.StudentMapper;
import com.training.centermanagement.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    /**
     * 统一登录验证
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
     * 自动识别角色登录（学生/教师/管理员统一入口）
     * @param username 学号、教师号或管理员名
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
     * 修改密码
     * @param role        student / teacher
     * @param id          学号或教师号
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 提示信息
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

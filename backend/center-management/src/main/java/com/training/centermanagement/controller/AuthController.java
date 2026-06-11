package com.training.centermanagement.controller;

import com.training.centermanagement.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 账号登录（自动识别学生/教师）
     * Body: { id: number, password: string }
     * 后端自动查学生表和教师表，无需前端传 role
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> payload) {
        Integer id = payload.get("id") != null ? Integer.parseInt(payload.get("id").toString()) : null;
        String password = (String) payload.get("password");
        return authService.loginAuto(id, password);
    }

    /**
     * 修改密码（学生/教师）
     * Body: { role: "student"|"teacher", id: number, oldPassword: string, newPassword: string }
     */
    @PutMapping("/change-password")
    public String changePassword(@RequestBody Map<String, Object> payload) {
        String role = (String) payload.get("role");
        Integer id = payload.get("id") != null ? Integer.parseInt(payload.get("id").toString()) : null;
        String oldPassword = (String) payload.get("oldPassword");
        String newPassword = (String) payload.get("newPassword");
        return authService.changePassword(role, id, oldPassword, newPassword);
    }
}

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
     * 统一登录（自动识别学生/教师/管理员）
     * Body: { username: string, password: string }
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");
        return authService.loginAuto(username, password);
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

package com.training.centermanagement.controller;

import com.training.centermanagement.entity.Teacher;
import com.training.centermanagement.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin
public class TeacherController {
    @Autowired
    private TeacherService teacherService;

    @GetMapping
    public List<Teacher> getAllTeachers() {
        return teacherService.getAllTeachers();
    }

    // 固定路径必须在 {teacherId} 之前
    @GetMapping("/by-specialty")
    public List<Teacher> getTeachersBySpecialty(@RequestParam String keyword) {
        return teacherService.getTeachersBySpecialtyKeyword(keyword);
    }

    // 教师薪酬汇总
    @GetMapping("/salaries")
    public List<Map<String, Object>> getTeacherSalaries() {
        return teacherService.getTeacherSalaries();
    }

    @GetMapping("/{teacherId}")
    public Teacher getTeacherById(@PathVariable Integer teacherId) {
        return teacherService.getTeacherById(teacherId);
    }

    @PostMapping
    public String addTeacher(@RequestBody Teacher teacher) {
        return teacherService.addTeacher(teacher);
    }

    @PutMapping
    public String updateTeacher(@RequestBody Teacher teacher) {
        return teacherService.updateTeacher(teacher);
    }

    @DeleteMapping("/{teacherId}")
    public String deleteTeacher(@PathVariable Integer teacherId) {
        return teacherService.deleteTeacher(teacherId);
    }
}

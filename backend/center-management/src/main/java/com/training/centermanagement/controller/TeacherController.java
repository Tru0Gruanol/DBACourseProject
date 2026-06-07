package com.training.centermanagement.controller;

import com.training.centermanagement.entity.Teacher;
import com.training.centermanagement.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
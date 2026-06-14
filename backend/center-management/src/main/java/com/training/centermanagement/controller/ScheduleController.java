package com.training.centermanagement.controller;

import com.training.centermanagement.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/student/{studentId}")
    public List<Map<String, Object>> getStudentSchedule(@PathVariable Integer studentId) {
        return scheduleService.getStudentSchedule(studentId);
    }

    @GetMapping("/teacher/{teacherId}")
    public List<Map<String, Object>> getTeacherSchedule(@PathVariable Integer teacherId) {
        return scheduleService.getTeacherSchedule(teacherId);
    }
}
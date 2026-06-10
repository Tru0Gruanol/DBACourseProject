package com.training.centermanagement.controller;

import com.training.centermanagement.entity.StudentEnrollment;
import com.training.centermanagement.mapper.StudentEnrollmentMapper;
import com.training.centermanagement.service.StudentEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin
public class StudentEnrollmentController {

    @Autowired
    private StudentEnrollmentService enrollmentService;
    @Autowired
    private StudentEnrollmentMapper enrollmentMapper;

    @PostMapping("/submit")
    public String submitEnrollment(@RequestBody Map<String, Object> payload) {
        Integer studentId = Integer.parseInt(payload.get("studentId").toString());
        String classCode = (String) payload.get("classCode");
        BigDecimal payment = new BigDecimal(payload.get("payment").toString());

        return enrollmentService.processEnrollment(studentId, classCode, payment);
    }

    @DeleteMapping("/cancel")
    public String cancelEnrollment(@RequestParam Integer studentId, @RequestParam String classCode) {
        return enrollmentService.cancelEnrollment(studentId, classCode);
    }

    // 检查报名状态（供前端判断是否为重新报名）
    @GetMapping("/check")
    public Map<String, Object> checkEnrollment(@RequestParam Integer studentId, @RequestParam String classCode) {
        Map<String, Object> result = new HashMap<>();
        StudentEnrollment any = enrollmentMapper.selectAnyByStudentAndClass(studentId, classCode);
        if (any != null) {
            result.put("exists", true);
            result.put("status", any.getStatus());
            result.put("enrollmentTime", any.getEnrollmentTime());
        } else {
            result.put("exists", false);
            result.put("status", null);
        }
        return result;
    }
}
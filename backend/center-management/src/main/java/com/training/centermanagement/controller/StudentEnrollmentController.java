package com.training.centermanagement.controller;

import com.training.centermanagement.service.StudentEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin
public class StudentEnrollmentController {

    @Autowired
    private StudentEnrollmentService enrollmentService;

    @PostMapping("/submit")
    public String submitEnrollment(@RequestBody Map<String, Object> payload) {
        Integer studentId = (Integer) payload.get("studentId");
        String classCode = (String) payload.get("classCode");
        BigDecimal payment = new BigDecimal(payload.get("payment").toString());

        return enrollmentService.processEnrollment(studentId, classCode, payment);
    }
}
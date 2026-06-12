package com.training.centermanagement.controller;

import com.training.centermanagement.entity.StudentEnrollment;
import com.training.centermanagement.mapper.StudentEnrollmentMapper;
import com.training.centermanagement.service.StudentEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
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

    // 学生申请退课（进入待审批）
    @PostMapping("/request-cancel")
    public String requestCancel(@RequestParam Integer studentId, @RequestParam String classCode) {
        return enrollmentService.requestCancel(studentId, classCode);
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

    // 查询某班级的在读学生列表（教师端查看用）
    @GetMapping("/students")
    public List<Map<String, Object>> getStudentsByClassCode(@RequestParam String classCode) {
        return enrollmentMapper.getActiveStudentsByClassCode(classCode);
    }

    // 查询所有待审批的退课申请
    @GetMapping("/pending-cancels")
    public List<Map<String, Object>> getPendingCancels() {
        return enrollmentService.getPendingCancels();
    }

    // 审批通过退课
    @PutMapping("/approve-cancel")
    public String approveCancel(@RequestParam Integer studentId, @RequestParam String classCode) {
        return enrollmentService.approveCancel(studentId, classCode);
    }

    // 拒绝退课
    @PutMapping("/reject-cancel")
    public String rejectCancel(@RequestParam Integer studentId, @RequestParam String classCode) {
        return enrollmentService.rejectCancel(studentId, classCode);
    }
}
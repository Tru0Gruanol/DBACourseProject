package com.training.centermanagement.service;

import com.training.centermanagement.entity.Account;
import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.entity.StudentEnrollment;
import com.training.centermanagement.entity.Subject;
import com.training.centermanagement.entity.Teacher;
import com.training.centermanagement.entity.Student;
import com.training.centermanagement.entity.Subject;
import com.training.centermanagement.mapper.AccountMapper;
import com.training.centermanagement.mapper.ClassesMapper;
import com.training.centermanagement.mapper.StudentEnrollmentMapper;
import com.training.centermanagement.mapper.StudentMapper;
import com.training.centermanagement.mapper.SubjectMapper;
import com.training.centermanagement.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 账目与缴费业务服务。
 *
 * 核心职责：
 * <ul>
 *   <li>缴费（makePayment）：学生总维度校验 → 写入流水 → 更新缓存金额 → 通知管理员</li>
 *   <li>收费清单（getInvoice）：单班级维度的应缴/已缴/欠费 + 明细</li>
 *   <li>学生汇总（getStudentSummary）：跨班级的缴费全貌</li>
 *   <li>催费（getDebtors）：欠费学生列表</li>
 *   <li>退款（refund）：负向冲销，保留审计轨迹</li>
 * </ul>
 */
@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private ClassesMapper classesMapper;
    @Autowired
    private StudentEnrollmentMapper studentEnrollmentMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private SubjectMapper subjectMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private NotificationService notificationService;

    public List<Account> getAllAccounts() {
        return accountMapper.getAllAccounts();
    }

    /**
     * 单独缴费（补缴）。
     * 执行学生总维度校验：累计实缴 + 本次缴费 ≤ 全部在读课程学费之和。
     * 成功后写入 accounts 流水并同步更新 student_enrollments.amount_paid 缓存。
     */
    @Transactional(rollbackFor = Exception.class)
    public String makePayment(Integer studentId, String classCode, BigDecimal amount) {
        // 1. 校验报名存在且为在读状态
        StudentEnrollment enrollment = studentEnrollmentMapper.selectByStudentAndClass(studentId, classCode);
        if (enrollment == null) {
            return "缴费失败：该学生未报名此班级或已退课！";
        }
        // 2. 校验基本金额
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "缴费失败：缴费金额必须大于0！";
        }
        // 3. 查询班级
        Classes cls = classesMapper.getClassByCode(classCode);
        if (cls == null) {
            return "缴费失败：班级不存在！";
        }
        // 4. 【学生总维度校验】累计实缴 + 本次缴费 ≤ 全部在读课程学费之和
        java.util.List<StudentEnrollment> activeList = studentEnrollmentMapper.getActiveEnrollmentsByStudentId(studentId);
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (StudentEnrollment e : activeList) {
            Classes c = classesMapper.getClassByCode(e.getClassCode());
            if (c != null) {
                totalFee = totalFee.add(c.getFee());
            }
            totalPaid = totalPaid.add(e.getAmountPaid());
        }
        if (totalPaid.add(amount).compareTo(totalFee) > 0) {
            return "缴费失败：累计缴费将超过应缴总额（应缴: " + totalFee + "，已缴: " + totalPaid + "，本次: " + amount + "）！";
        }
        // 5. 写入流水
        Account account = new Account();
        account.setAccountDate(new Date());
        account.setClassCode(classCode);
        account.setStudentId(studentId);
        account.setSubjectId(cls.getSubjectId());
        account.setAmountPaid(amount);
        accountMapper.insertAccount(account);
        // 6. 更新报名表的已缴金额缓存
        int rows = accountMapper.updateEnrollmentAmountPaid(studentId, classCode, amount);
        if (rows == 0) {
            return "缴费失败：更新选课金额失败！";
        }

        // 7. 通知管理员
        Student student = studentMapper.selectStudentById(studentId);
        Subject subject = subjectMapper.getSubjectById(cls.getSubjectId());
        String studentName = student != null ? student.getStudentName() : String.valueOf(studentId);
        String subjectName = subject != null ? subject.getSubjectName() : "";

        // 计算本班级是否已结清
        BigDecimal classPaidBefore = enrollment.getAmountPaid();
        BigDecimal classPaidAfter = classPaidBefore.add(amount);
        BigDecimal classRemaining = cls.getFee().subtract(classPaidAfter);

        if (classRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            notificationService.send(0, "admin",
                "🎉 缴费结清",
                studentName + " 已结清 " + classCode + "（" + subjectName + "）全部费用 ¥" + cls.getFee());
        } else {
            notificationService.send(0, "admin",
                "💰 部分缴费",
                studentName + " 为 " + classCode + "（" + subjectName + "）缴纳 ¥" + amount + "，尚欠 ¥" + classRemaining);
        }

        return "缴费成功！已缴纳 " + amount + " 元。";
    }

    // 收费清单
    public Map<String, Object> getInvoice(Integer studentId, String classCode) {
        Map<String, Object> invoice = new HashMap<>();
        Classes cls = classesMapper.getClassByCode(classCode);
        if (cls == null) {
            invoice.put("error", "班级不存在");
            return invoice;
        }
        BigDecimal totalFee = cls.getFee();
        List<Account> accounts = accountMapper.getAccountsByStudentAndClass(studentId, classCode);
        StudentEnrollment enrollment = studentEnrollmentMapper.selectByStudentAndClass(studentId, classCode);
        BigDecimal totalPaid = enrollment != null ? enrollment.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal debt = totalFee.subtract(totalPaid);
        invoice.put("studentId", studentId);
        invoice.put("classCode", classCode);
        invoice.put("className", cls.getTerm() + " " + cls.getClassCode());
        invoice.put("totalFee", totalFee);
        invoice.put("totalPaid", totalPaid);
        invoice.put("debt", debt);
        invoice.put("payments", accounts);
        return invoice;
    }

    // 学生缴费总览（统一缴费查询页/已选课程页用）
    public Map<String, Object> getStudentSummary(Integer studentId) {
        Map<String, Object> summary = new HashMap<>();
        // 全部报名记录（含已退课）
        List<StudentEnrollment> allEnrollments = studentEnrollmentMapper.getAllEnrollmentsByStudentId(studentId);
        // 全部流水
        List<Account> allAccounts = new java.util.ArrayList<>();
        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;

        List<Map<String, Object>> enrollmentDetails = new ArrayList<>();
        for (StudentEnrollment e : allEnrollments) {
            Classes cls = classesMapper.getClassByCode(e.getClassCode());
            if (cls == null) continue;
            BigDecimal fee = cls.getFee();
            // 计算该报名累计实缴
            BigDecimal paid = accountMapper.getTotalPaidByStudentAndClass(studentId, e.getClassCode());
            boolean isActive = "active".equals(e.getStatus());

            // 查询科目名
            Subject subject = subjectMapper.getSubjectById(cls.getSubjectId());
            String subjectName = subject != null ? subject.getSubjectName() : "";

            // 查询教师信息
            Teacher teacher = teacherMapper.getTeacherById(cls.getTeacherId());
            String teacherName = teacher != null ? teacher.getTeacherName() : "";
            String teacherLevel = teacher != null ? teacher.getTeacherLevel() : "";

            Map<String, Object> detail = new HashMap<>();
            detail.put("classCode", e.getClassCode());
            detail.put("subjectId", cls.getSubjectId());
            detail.put("subjectName", subjectName);
            detail.put("teacherId", cls.getTeacherId());
            detail.put("teacherName", teacherName);
            detail.put("teacherLevel", teacherLevel);
            detail.put("term", cls.getTerm());
            detail.put("period", cls.getPeriod());
            detail.put("location", cls.getLocation());
            detail.put("fee", fee);
            detail.put("totalPaid", paid);
            detail.put("status", e.getStatus());  // 'active' or 'cancelled'
            detail.put("enrollmentTime", e.getEnrollmentTime());
            enrollmentDetails.add(detail);

            if (isActive) {
                totalFee = totalFee.add(fee);
            }
            totalPaid = totalPaid.add(paid);

            // 收集该报名下的流水
            allAccounts.addAll(accountMapper.getAccountsByStudentAndClass(studentId, e.getClassCode()));
        }

        summary.put("enrollments", enrollmentDetails);
        summary.put("totalFee", totalFee);    // 仅统计 active 课程
        summary.put("totalPaid", totalPaid);  // 全部流水净额
        summary.put("accounts", allAccounts);
        return summary;
    }

    // 催费列表
    public List<Map<String, Object>> getDebtors() {
        return accountMapper.getDebtors();
    }

    // 查询某学生+某班级的累计实缴总额
    public BigDecimal getTotalPaid(Integer studentId, String classCode) {
        return accountMapper.getTotalPaidByStudentAndClass(studentId, classCode);
    }

    /**
     * 退款：创建一条负向流水记录。
     * 不会 DELETE 原缴费记录——accounts 表为不可变审计日志，
     * 通过 SUM(amount_paid) 在任何时间点都可得到正确的净缴费额。
     */
    @Transactional(rollbackFor = Exception.class)
    public String refund(Integer studentId, String classCode, BigDecimal amount) {
        StudentEnrollment enrollment = studentEnrollmentMapper.selectByStudentAndClass(studentId, classCode);
        if (enrollment == null) {
            return "退款失败：该学生未报名此班级！";
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "退款失败：退款金额必须大于0！";
        }

        Classes cls = classesMapper.getClassByCode(classCode);
        if (cls == null) {
            return "退款失败：班级不存在！";
        }

        // 当前累计实缴
        BigDecimal totalPaid = accountMapper.getTotalPaidByStudentAndClass(studentId, classCode);
        if (amount.compareTo(totalPaid) > 0) {
            return "退款失败：退款金额 (" + amount + ") 超过了已缴总额 (" + totalPaid + ")！";
        }

        // 插入负向流水作为退款记录
        Account refundAccount = new Account();
        refundAccount.setAccountDate(new Date());
        refundAccount.setClassCode(classCode);
        refundAccount.setStudentId(studentId);
        refundAccount.setSubjectId(cls.getSubjectId());
        refundAccount.setAmountPaid(amount.negate()); // 负数表示退款
        accountMapper.insertAccount(refundAccount);

        // 同步更新 student_enrollments 中的已缴金额
        accountMapper.updateEnrollmentAmountPaid(studentId, classCode, amount.negate());

        return "退款成功！已退还 " + amount + " 元。";
    }
}
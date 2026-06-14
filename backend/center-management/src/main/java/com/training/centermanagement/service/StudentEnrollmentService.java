package com.training.centermanagement.service;

import com.training.centermanagement.entity.Account;
import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.entity.Student;
import com.training.centermanagement.entity.StudentEnrollment;
import com.training.centermanagement.entity.Subject;
import com.training.centermanagement.entity.Teacher;
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
 * 学生报名与退课业务服务。
 *
 * 核心职责：
 * <ul>
 *   <li>报名事务（processEnrollment）：七重门禁 + 双表原子写入</li>
 *   <li>管理端退课：验证报名 → 全额退款（负向冲销）→ 软删除</li>
 *   <li>学生申请退课 + 管理员审批（通过/拒绝）</li>
 * </ul>
 *
 * @see AccountService      账目相关的缴费/冲销操作
 * @see NotificationService 退课审批的通知发送
 */
@Service
public class StudentEnrollmentService {

    @Autowired
    private ClassesMapper classesMapper;
    @Autowired
    private StudentEnrollmentMapper studentEnrollmentMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private SubjectMapper subjectMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private NotificationService notificationService;

    /**
     * 学生报名核心事务。
     *
     * 执行七重门禁校验后，原子性地完成报名登记与账目录入：
     * <ol>
     *   <li>学生存在性校验</li>
     *   <li>同班级防重复（已在读则拒绝）</li>
     *   <li>cancelled 记录复用检测（重新报名走 UPDATE）</li>
     *   <li>班级存在性校验</li>
     *   <li>同科目防重复（遍历 active 报名比对 subject_id）</li>
     *   <li>满员检查 + 学生总维度缴费校验</li>
     *   <li>原子性名额递增（WHERE enrolled_count &lt; capacity 防超卖）</li>
     * </ol>
     *
     * @param studentId 学生编号
     * @param classCode 班级代号
     * @param payment   本次缴费金额（可为 0）
     * @return 操作结果中文提示
     */
    @Transactional(rollbackFor = Exception.class)
    public String processEnrollment(Integer studentId, String classCode, BigDecimal payment) {
        // 1. 检查学生是否存在
        Student student = studentMapper.selectStudentById(studentId);
        if (student == null) {
            return "报名失败：学生ID " + studentId + " 不存在！";
        }

        // 2. 检查是否已在读
        StudentEnrollment activeEnrollment = studentEnrollmentMapper.selectByStudentAndClass(studentId, classCode);
        if (activeEnrollment != null) {
            return "报名失败：您已经报名过该班级，不能重复报名！";
        }

        // 3. 检查是否曾退过该课程（cancelled 记录可复用）
        StudentEnrollment cancelledEnrollment = studentEnrollmentMapper.selectAnyByStudentAndClass(studentId, classCode);
        boolean isReEnroll = (cancelledEnrollment != null);  // true = 重新报名

        // 4. 查询班级信息
        Classes cls = classesMapper.getClassByCode(classCode);
        if (cls == null) {
            return "报名失败：班级不存在！";
        }

        // 4.5. 【同科目防重复】检查学生是否已报名该科目的其他班级
        java.util.List<StudentEnrollment> activeList = studentEnrollmentMapper.getActiveEnrollmentsByStudentId(studentId);
        for (StudentEnrollment e : activeList) {
            Classes existingClass = classesMapper.getClassByCode(e.getClassCode());
            if (existingClass != null && existingClass.getSubjectId().equals(cls.getSubjectId())) {
                return "报名失败：该学生已报名同一科目（subject_id=" + cls.getSubjectId() + "）的班级 " + e.getClassCode() + "，不能重复报名同一科目！";
            }
        }

        // 5. 检查是否满员
        if (cls.getEnrolledCount() >= cls.getCapacity()) {
            return "报名失败：【" + cls.getClassCode() + "】已满员！系统建议引导学员选择同科目下一期班级。";
        }

        // 6. 【学生总维度校验】本次缴费 + 已有累计缴费 ≤ 已有课程学费 + 新课程学费
        BigDecimal totalFee = cls.getFee();  // 包含新课程学费
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (StudentEnrollment e : activeList) {
            Classes c = classesMapper.getClassByCode(e.getClassCode());
            if (c != null) {
                totalFee = totalFee.add(c.getFee());
            }
            totalPaid = totalPaid.add(e.getAmountPaid());
        }
        if (totalPaid.add(payment).compareTo(totalFee) > 0) {
            return "报名失败：缴费金额超过应缴总额（应缴: " + totalFee + "，已缴: " + totalPaid + "，本次: " + payment + "）！";
        }

        // 7. 原子性增加班级已报名人数（防止超卖）
        int updateRows = classesMapper.incrementEnrolledCount(classCode);
        if (updateRows == 0) {
            return "报名失败：名额刚刚被抢光！";
        }

        // 8. 写入选课桥接表（重新报名则 UPDATE，新报名则 INSERT）
        Date now = new Date();
        if (isReEnroll) {
            studentEnrollmentMapper.reEnroll(studentId, classCode, payment, now);
        } else {
            StudentEnrollment enrollment = new StudentEnrollment();
            enrollment.setStudentId(studentId);
            enrollment.setClassCode(classCode);
            enrollment.setEnrollmentTime(now);
            enrollment.setAmountPaid(payment);
            studentEnrollmentMapper.insertEnrollment(enrollment);
        }

        // 9. 写入财务明细流水表
        Account account = new Account();
        account.setAccountDate(now);
        account.setClassCode(classCode);
        account.setStudentId(studentId);
        account.setSubjectId(cls.getSubjectId());
        account.setAmountPaid(payment);
        accountMapper.insertAccount(account);

        // 10. 发送通知
        Subject subject = subjectMapper.getSubjectById(cls.getSubjectId());
        String subjectName = subject != null ? subject.getSubjectName() : "";
        Teacher teacher = teacherMapper.getTeacherById(cls.getTeacherId());
        String teacherName = teacher != null ? teacher.getTeacherName() : "";

        // 通知管理员
        if (payment.compareTo(BigDecimal.ZERO) > 0) {
            notificationService.send(0, "admin",
                "📋 新报名",
                student.getStudentName() + " 已报名 " + classCode + "（" + subjectName + "）并缴纳 ¥" + payment);
        } else {
            notificationService.send(0, "admin",
                "📋 新报名（未缴费）",
                student.getStudentName() + " 已报名 " + classCode + "（" + subjectName + "），尚未缴费");
        }

        // 通知教师
        notificationService.send(cls.getTeacherId(), "teacher",
            "👤 新学员",
            student.getStudentName() + " 已加入您的班级 " + classCode + "（" + subjectName + "）");

        // 管理员代报名 → 通知学生
        // 判断依据：学生端调用时 AuthController 注入的 userId 与学生 ID 一致；管理端则不一致
        // 此处无法直接区分，但即便多发一条也不影响——学生端自己报名时收到"管理员已为您报名"也不违和
        notificationService.send(studentId, "student",
            "📝 报名成功",
            "您已成功报名 " + classCode + "（" + subjectName + "），授课教师：" + teacherName + "，学费 ¥" + cls.getFee());

        // 满员检测
        if (cls.getEnrolledCount() + 1 >= cls.getCapacity()) {
            notificationService.send(0, "admin",
                "🈵 班级满员",
                classCode + "（" + subjectName + "）已满员（" + (cls.getEnrolledCount() + 1) + "/" + cls.getCapacity() + "）");
        }

        return "报名成功！多表事务已提交。已交款：" + payment + " 元。";
    }

    /**
     * 管理端直接退课。
     * 验证报名存在后：全额退款（负向冲销）+ 软删除 + 恢复班级名额 + 通知学生。
     * 退款通过 accounts 表负向记录实现，保留完整审计轨迹。
     */
    @Transactional(rollbackFor = Exception.class)
    public String cancelEnrollment(Integer studentId, String classCode) {
        StudentEnrollment enrollment = studentEnrollmentMapper.selectByStudentAndClass(studentId, classCode);
        if (enrollment == null) return "退课失败：该学生未报名此班级或已退课！";

        Classes cls = classesMapper.getClassByCode(classCode);
        if (cls == null) return "退课失败：班级不存在！";

        BigDecimal totalPaid = accountMapper.getTotalPaidByStudentAndClass(studentId, classCode);
        if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            Account refund = new Account();
            refund.setAccountDate(new Date());
            refund.setClassCode(classCode);
            refund.setStudentId(studentId);
            refund.setSubjectId(cls.getSubjectId());
            refund.setAmountPaid(totalPaid.negate());
            accountMapper.insertAccount(refund);
            accountMapper.updateEnrollmentAmountPaid(studentId, classCode, totalPaid.negate());
        }
        studentEnrollmentMapper.updateStatus(studentId, classCode, "cancelled");
        classesMapper.decrementEnrolledCount(classCode);

        // 通知学生
        notificationService.send(studentId, "student",
            "退课通知", "管理员已将您从班级 " + classCode + " 退课，已退款 " + totalPaid + " 元。");
        // 通知教师
        Student student = studentMapper.selectStudentById(studentId);
        String studentName = student != null ? student.getStudentName() : String.valueOf(studentId);
        notificationService.send(cls.getTeacherId(), "teacher",
            "👋 学员退课", studentName + " 已退出您的班级 " + classCode + "，当前在读 " + (cls.getEnrolledCount() - 1) + "/" + cls.getCapacity() + " 人");
        return "退课成功！已退款 " + totalPaid + " 元。";
    }

    /**
     * 学生申请退课。
     * 将报名状态标记为 pending_cancel 并通知管理员审批，
     * 而非直接退款——模拟真实业务中的审批流程。
     */
    public String requestCancel(Integer studentId, String classCode) {
        StudentEnrollment enrollment = studentEnrollmentMapper.selectByStudentAndClass(studentId, classCode);
        if (enrollment == null) return "申请失败：该学生未报名此班级或已退课！";

        studentEnrollmentMapper.updateStatus(studentId, classCode, "pending_cancel");
        Classes cls = classesMapper.getClassByCode(classCode);
        String className = cls != null ? cls.getClassCode() : classCode;
        notificationService.send(0, "admin",
            "退课申请", "学生 " + studentId + " 申请退出班级 " + className + "，请到收费管理审批");
        return "退课申请已提交，等待管理员审批。";
    }

    /** 管理员审批通过退课申请 → 执行退款 + 通知学生。与 cancelEnrollment 逻辑一致。 */
    @Transactional(rollbackFor = Exception.class)
    public String approveCancel(Integer studentId, String classCode) {
        StudentEnrollment enrollment = studentEnrollmentMapper.selectAnyByStudentAndClass(studentId, classCode);
        if (enrollment == null || !"pending_cancel".equals(enrollment.getStatus())) {
            return "审批失败：无待审批的退课申请！";
        }
        Classes cls = classesMapper.getClassByCode(classCode);
        BigDecimal totalPaid = accountMapper.getTotalPaidByStudentAndClass(studentId, classCode);
        if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            Account refundAccount = new Account();
            refundAccount.setAccountDate(new Date());
            refundAccount.setClassCode(classCode);
            refundAccount.setStudentId(studentId);
            refundAccount.setSubjectId(cls != null ? cls.getSubjectId() : 0);
            refundAccount.setAmountPaid(totalPaid.negate());
            accountMapper.insertAccount(refundAccount);
            accountMapper.updateEnrollmentAmountPaid(studentId, classCode, totalPaid.negate());
        }
        studentEnrollmentMapper.updateStatus(studentId, classCode, "cancelled");
        classesMapper.decrementEnrolledCount(classCode);
        // 通知学生
        notificationService.send(studentId, "student",
            "退课已通过", "您退出班级 " + classCode + " 的申请已审批通过，已退款 " + totalPaid + " 元。");
        // 通知教师
        Student stu = studentMapper.selectStudentById(studentId);
        String stuName = stu != null ? stu.getStudentName() : String.valueOf(studentId);
        notificationService.send(cls.getTeacherId(), "teacher",
            "👋 学员退课", stuName + " 已退出您的班级 " + classCode + "，当前在读 " + (cls.getEnrolledCount() - 1) + "/" + cls.getCapacity() + " 人");
        return "审批通过，已退款 " + totalPaid + " 元。";
    }

    /** 管理员拒绝退课申请 → 恢复 active 状态 + 通知学生。无需操作账目。 */
    public String rejectCancel(Integer studentId, String classCode) {
        StudentEnrollment enrollment = studentEnrollmentMapper.selectAnyByStudentAndClass(studentId, classCode);
        if (enrollment == null || !"pending_cancel".equals(enrollment.getStatus())) {
            return "审批失败：无待审批的退课申请！";
        }
        studentEnrollmentMapper.updateStatus(studentId, classCode, "active");
        notificationService.send(studentId, "student",
            "退课被拒绝", "您退出班级 " + classCode + " 的申请已被管理员拒绝，如有疑问请联系培训中心。");
        return "已拒绝该退课申请。";
    }

    /** 查询所有待审批的退课申请，组装含学生姓名、班级信息、实缴总额的视图对象。 */
    public List<Map<String, Object>> getPendingCancels() {
        List<StudentEnrollment> list = studentEnrollmentMapper.getPendingCancels();
        List<Map<String, Object>> result = new ArrayList<>();
        for (StudentEnrollment e : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", e.getStudentId());
            m.put("classCode", e.getClassCode());
            m.put("enrollmentTime", e.getEnrollmentTime());
            m.put("amountPaid", e.getAmountPaid());
            // 查学生姓名
            Student s = studentMapper.selectStudentById(e.getStudentId());
            m.put("studentName", s != null ? s.getStudentName() : "");
            // 查班级信息
            Classes c = classesMapper.getClassByCode(e.getClassCode());
            m.put("fee", c != null ? c.getFee() : BigDecimal.ZERO);
            m.put("subjectId", c != null ? c.getSubjectId() : null);
            // 查实缴总额
            m.put("totalPaid", accountMapper.getTotalPaidByStudentAndClass(e.getStudentId(), e.getClassCode()));
            result.add(m);
        }
        return result;
    }
}
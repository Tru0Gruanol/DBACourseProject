package com.training.centermanagement.service;

import com.training.centermanagement.entity.Account;
import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.entity.Student;
import com.training.centermanagement.entity.StudentEnrollment;
import com.training.centermanagement.mapper.AccountMapper;
import com.training.centermanagement.mapper.ClassesMapper;
import com.training.centermanagement.mapper.StudentEnrollmentMapper;
import com.training.centermanagement.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class StudentEnrollmentService {

    @Autowired
    private ClassesMapper classesMapper;
    @Autowired
    private StudentEnrollmentMapper studentEnrollmentMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private StudentMapper studentMapper;   // 用于检查学生是否存在

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

        // 5. 检查是否满员
        if (cls.getEnrolledCount() >= cls.getCapacity()) {
            return "报名失败：【" + cls.getClassCode() + "】已满员！系统建议引导学员选择同科目下一期班级。";
        }

        // 6. 【学生总维度校验】本次缴费 + 已有累计缴费 ≤ 已有课程学费 + 新课程学费
        java.util.List<StudentEnrollment> activeList = studentEnrollmentMapper.getActiveEnrollmentsByStudentId(studentId);
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

        return "报名成功！多表事务已提交。已交款：" + payment + " 元。";
    }

    @Transactional(rollbackFor = Exception.class)
    public String cancelEnrollment(Integer studentId, String classCode) {
        // 1. 检查报名是否存在且为在读状态
        StudentEnrollment enrollment = studentEnrollmentMapper.selectByStudentAndClass(studentId, classCode);
        if (enrollment == null) {
            return "退课失败：该学生未报名此班级或已退课！";
        }

        // 2. 查询该报名的累计实缴总额（含历史退款冲销后的净值）
        BigDecimal totalPaid = accountMapper.getTotalPaidByStudentAndClass(studentId, classCode);

        // 3. 查询班级应缴学费
        Classes cls = classesMapper.getClassByCode(classCode);
        if (cls == null) {
            return "退课失败：班级不存在！";
        }
        BigDecimal fee = cls.getFee();

        // 4. 退课即退款：退还本课程的全部已缴金额
        String refundMsg = "";
        if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            // 全额退款：插入负向冲销流水
            Account refundAccount = new Account();
            refundAccount.setAccountDate(new Date());
            refundAccount.setClassCode(classCode);
            refundAccount.setStudentId(studentId);
            refundAccount.setSubjectId(cls.getSubjectId());
            refundAccount.setAmountPaid(totalPaid.negate()); // 全额负向冲销
            accountMapper.insertAccount(refundAccount);

            // 同步更新 enrollment 的 amount_paid（归零）
            accountMapper.updateEnrollmentAmountPaid(studentId, classCode, totalPaid.negate());

            refundMsg = "，已退款 " + totalPaid + " 元（本课程报名费全额退还）";
        }

        // 5. 标记报名为已退课（不再物理删除）
        studentEnrollmentMapper.cancelEnrollment(studentId, classCode);

        // 6. 释放班级名额
        classesMapper.decrementEnrolledCount(classCode);

        return "退课成功！学生 " + studentId + " 已从班级 " + classCode + " 退出。" + refundMsg;
    }
}
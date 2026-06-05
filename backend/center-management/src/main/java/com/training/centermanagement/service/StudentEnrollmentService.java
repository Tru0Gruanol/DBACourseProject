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

        // 2. 检查是否已经报名过该班级
        StudentEnrollment existing = studentEnrollmentMapper.selectByStudentAndClass(studentId, classCode);
        if (existing != null) {
            return "报名失败：您已经报名过该班级，不能重复报名！";
        }

        // 3. 查询班级信息
        Classes cls = classesMapper.getClassByCode(classCode);
        if (cls == null) {
            return "报名失败：班级不存在！";
        }

        // 4. 检查是否满员
        if (cls.getEnrolledCount() >= cls.getCapacity()) {
            return "报名失败：【" + cls.getClassCode() + "】已满员！系统建议引导学员选择同科目下一期班级。";
        }

        // 5. 原子性增加班级已报名人数（防止超卖）
        int updateRows = classesMapper.incrementEnrolledCount(classCode);
        if (updateRows == 0) {
            return "报名失败：名额刚刚被抢光！";
        }

        // 6. 写入选课桥接表
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setClassCode(classCode);
        enrollment.setEnrollmentTime(new Date());
        enrollment.setAmountPaid(payment);
        studentEnrollmentMapper.insertEnrollment(enrollment);

        // 7. 写入财务明细流水表
        Account account = new Account();
        account.setAccountDate(new Date());
        account.setClassCode(classCode);
        account.setStudentId(studentId);
        account.setSubjectId(cls.getSubjectId());
        account.setAmountPaid(payment);
        accountMapper.insertAccount(account);

        return "报名成功！多表事务已提交。已交款：" + payment + " 元。";
    }
}
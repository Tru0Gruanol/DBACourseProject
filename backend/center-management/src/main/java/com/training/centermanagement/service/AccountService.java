package com.training.centermanagement.service;

import com.training.centermanagement.entity.Account;
import com.training.centermanagement.entity.Classes;
import com.training.centermanagement.entity.StudentEnrollment;
import com.training.centermanagement.mapper.AccountMapper;
import com.training.centermanagement.mapper.ClassesMapper;
import com.training.centermanagement.mapper.StudentEnrollmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private ClassesMapper classesMapper;
    @Autowired
    private StudentEnrollmentMapper studentEnrollmentMapper;

    public List<Account> getAllAccounts() {
        return accountMapper.getAllAccounts();
    }

    // 单独缴费
    @Transactional(rollbackFor = Exception.class)
    public String makePayment(Integer studentId, String classCode, BigDecimal amount) {
        StudentEnrollment enrollment = studentEnrollmentMapper.selectByStudentAndClass(studentId, classCode);
        if (enrollment == null) {
            return "缴费失败：该学生未报名此班级！";
        }
        Classes cls = classesMapper.getClassByCode(classCode);
        if (cls == null) {
            return "缴费失败：班级不存在！";
        }
        Account account = new Account();
        account.setAccountDate(new Date());
        account.setClassCode(classCode);
        account.setStudentId(studentId);
        account.setSubjectId(cls.getSubjectId());
        account.setAmountPaid(amount);
        accountMapper.insertAccount(account);

        int rows = accountMapper.updateEnrollmentAmountPaid(studentId, classCode, amount);
        if (rows == 0) {
            return "缴费失败：更新选课金额失败！";
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

    // 催费列表
    public List<Map<String, Object>> getDebtors() {
        return accountMapper.getDebtors();
    }
}
package com.training.centermanagement.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Account {
    private Integer accountId;
    private Date accountDate;
    private String classCode;
    private Integer studentId;
    private Integer subjectId;
    private BigDecimal amountPaid;

    public Account() {}

    // Getter and Setter
    public Integer getAccountId() {
        return accountId;
    }
    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }
    public Date getAccountDate() {
        return accountDate;
    }
    public void setAccountDate(Date accountDate) {
        this.accountDate = accountDate;
    }
    public String getClassCode() {
        return classCode;
    }
    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }
    public Integer getStudentId() {
        return studentId;
    }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    public Integer getSubjectId() {
        return subjectId;
    }
    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }
    public BigDecimal getAmountPaid() {
        return amountPaid;
    }
    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }
}
package com.training.centermanagement.entity;

import java.math.BigDecimal;
import java.util.Date;

public class StudentEnrollment {
    private Integer studentId;
    private String classCode;
    private Date enrollmentTime;
    private BigDecimal amountPaid;

    public StudentEnrollment() {}

    public Integer getStudentId() {
        return studentId;
    }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    public String getClassCode() {
        return classCode;
    }
    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }
    public Date getEnrollmentTime() {
        return enrollmentTime;
    }
    public void setEnrollmentTime(Date enrollmentTime) {
        this.enrollmentTime = enrollmentTime;
    }
    public BigDecimal getAmountPaid() {
        return amountPaid;
    }
    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }
}
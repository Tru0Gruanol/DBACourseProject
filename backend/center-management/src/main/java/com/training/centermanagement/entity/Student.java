package com.training.centermanagement.entity;

import java.util.Date;

public class Student {
    private Integer studentId;
    private String studentName;
    private Date registrationTime;

    public Student() {}

    public Integer getStudentId() {
        return studentId;
    }
    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }
    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    public Date getRegistrationTime() {
        return registrationTime;
    }
    public void setRegistrationTime(Date registrationTime) {
        this.registrationTime = registrationTime;
    }
}
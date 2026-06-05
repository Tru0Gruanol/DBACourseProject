package com.training.centermanagement.entity;

import java.math.BigDecimal;

public class Classes {
    private String classCode;
    private Integer subjectId;
    private Integer teacherId;
    private String term;
    private String period;
    private BigDecimal fee;
    private String location;
    private Integer capacity;
    private Integer enrolledCount;
    private BigDecimal teacherRemuneration;

    // 无参构造器
    public Classes() {}

    // getter 和 setter
    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Integer teacherId) {
        this.teacherId = teacherId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(Integer enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public BigDecimal getTeacherRemuneration() {
        return teacherRemuneration;
    }

    public void setTeacherRemuneration(BigDecimal teacherRemuneration) {
        this.teacherRemuneration = teacherRemuneration;
    }
}
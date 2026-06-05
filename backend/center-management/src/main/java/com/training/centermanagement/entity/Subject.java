package com.training.centermanagement.entity;

public class Subject {
    private Integer subjectId;
    private String subjectName;
    private Integer hours;

    // 无参构造器
    public Subject() {}

    // getter 和 setter
    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }
}
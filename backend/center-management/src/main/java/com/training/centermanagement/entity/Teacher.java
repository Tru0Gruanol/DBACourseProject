package com.training.centermanagement.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Teacher {
    private Integer teacherId;
    private String teacherName;
    private String teacherLevel;
    private String specialty;
    private String password;
    /** 该教师任教的科目ID列表（不映射数据库字段，由 Service 层单独查询填充） */
    private List<Integer> subjectIds;
}
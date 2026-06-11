package com.training.centermanagement.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Teacher {
    private Integer teacherId;
    private String teacherName;
    private String teacherLevel;
    private String specialty;
    private String password;
}
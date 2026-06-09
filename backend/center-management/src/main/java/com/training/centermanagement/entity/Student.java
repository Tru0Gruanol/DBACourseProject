package com.training.centermanagement.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Student {
    private Integer studentId;
    private String studentName;
    private Date registrationTime;
}
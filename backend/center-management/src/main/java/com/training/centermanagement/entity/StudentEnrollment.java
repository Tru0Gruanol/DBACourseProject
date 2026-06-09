package com.training.centermanagement.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class StudentEnrollment {
    private Integer studentId;
    private String classCode;
    private Date enrollmentTime;
    private BigDecimal amountPaid;
}
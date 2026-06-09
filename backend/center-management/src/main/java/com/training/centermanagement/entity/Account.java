package com.training.centermanagement.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class Account {
    private Integer accountId;
    private Date accountDate;
    private String classCode;
    private Integer studentId;
    private Integer subjectId;
    private BigDecimal amountPaid;
}
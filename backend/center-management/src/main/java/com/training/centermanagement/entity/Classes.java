package com.training.centermanagement.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
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
}
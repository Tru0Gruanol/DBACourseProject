package com.training.centermanagement.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Subject {
    private Integer subjectId;
    private String subjectName;
    private Integer hours;
}
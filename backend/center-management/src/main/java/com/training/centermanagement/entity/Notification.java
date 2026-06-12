package com.training.centermanagement.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
public class Notification {
    private Integer id;
    private Integer userId;
    private String userRole;
    private String title;
    private String content;
    private Integer isRead;
    private Date createdAt;
}

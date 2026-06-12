package com.training.centermanagement.service;

import com.training.centermanagement.entity.Notification;
import com.training.centermanagement.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {
    @Autowired private NotificationMapper mapper;

    public List<Notification> getByUser(Integer userId, String role) {
        return mapper.getByUser(userId, role);
    }

    public int countUnread(Integer userId, String role) {
        return mapper.countUnread(userId, role);
    }

    public String markRead(Integer id) {
        return mapper.markRead(id) > 0 ? "ok" : "fail";
    }

    public String markAllRead(Integer userId, String role) {
        return mapper.markAllRead(userId, role) > 0 ? "ok" : "fail";
    }

    public void send(Integer userId, String role, String title, String content) {
        Notification n = new Notification();
        n.setUserId(userId); n.setUserRole(role);
        n.setTitle(title); n.setContent(content);
        mapper.insert(n);
    }
}

package com.training.centermanagement.controller;

import com.training.centermanagement.entity.Notification;
import com.training.centermanagement.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired private NotificationService service;

    @GetMapping
    public List<Notification> get(@RequestParam Integer userId, @RequestParam String role) {
        return service.getByUser(userId, role);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount(@RequestParam Integer userId, @RequestParam String role) {
        return Map.of("count", service.countUnread(userId, role));
    }

    @PutMapping("/{id}/read")
    public String markRead(@PathVariable Integer id) { return service.markRead(id); }

    @PutMapping("/read-all")
    public String markAllRead(@RequestParam Integer userId, @RequestParam String role) {
        return service.markAllRead(userId, role);
    }
}

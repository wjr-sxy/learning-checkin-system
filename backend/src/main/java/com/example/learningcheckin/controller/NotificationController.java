package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.SysNotice;
import com.example.learningcheckin.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private INotificationService notificationService;

    @GetMapping("/unread")
    public Result<List<SysNotice>> getUnreadNotifications(@RequestParam Long userId) {
        return Result.success(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/all")
    public Result<List<SysNotice>> getUserNotifications(@RequestParam Long userId) {
        return Result.success(notificationService.getUserNotifications(userId));
    }

    @PostMapping("/read/{id}")
    public Result<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<?> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
}

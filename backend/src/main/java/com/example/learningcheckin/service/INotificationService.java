package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.entity.SysNotice;
import java.util.List;

public interface INotificationService extends IService<SysNotice> {
    void sendNotification(Long userId, String title, String content, String type);
    List<SysNotice> getUnreadNotifications(Long userId);
    List<SysNotice> getUserNotifications(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
}

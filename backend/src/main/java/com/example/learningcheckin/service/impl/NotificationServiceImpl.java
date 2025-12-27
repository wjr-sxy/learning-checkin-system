package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.entity.SysNotice;
import com.example.learningcheckin.mapper.SysNoticeMapper;
import com.example.learningcheckin.service.INotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements INotificationService {

    @Override
    public void sendNotification(Long userId, String title, String content, String type) {
        SysNotice notification = new SysNotice();
        notification.setReceiverId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);
    }

    @Override
    public List<SysNotice> getUnreadNotifications(Long userId) {
        return this.list(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getReceiverId, userId)
                .eq(SysNotice::getIsRead, 0)
                .orderByDesc(SysNotice::getCreateTime));
    }

    @Override
    public List<SysNotice> getUserNotifications(Long userId) {
        return this.list(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getReceiverId, userId)
                .orderByDesc(SysNotice::getCreateTime));
    }

    @Override
    public void markAsRead(Long notificationId) {
        SysNotice notification = this.getById(notificationId);
        if (notification != null) {
            notification.setIsRead(1);
            this.updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        this.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysNotice>()
                .eq(SysNotice::getReceiverId, userId)
                .set(SysNotice::getIsRead, 1));
    }
}

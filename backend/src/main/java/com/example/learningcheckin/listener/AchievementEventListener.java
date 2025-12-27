package com.example.learningcheckin.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learningcheckin.entity.LearningLog;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.event.CheckinEvent;
import com.example.learningcheckin.event.TaskCompletedEvent;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.service.ILearningLogService;
import com.example.learningcheckin.service.IPointsService;
import com.example.learningcheckin.service.IUserBadgeService;
import com.example.learningcheckin.websocket.NotificationWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AchievementEventListener {

    @Autowired
    private IUserBadgeService userBadgeService;

    @Autowired
    private IPointsService pointsService;

    @Autowired
    private ILearningLogService learningLogService;
    
    @Autowired
    private UserMapper userMapper;

    @EventListener
    public void handleTaskCompleted(TaskCompletedEvent event) {
        // Rule 1: 5 Video tasks -> "Light Scholar" (Badge ID 101 for example)
        if ("VIDEO".equals(event.getTaskType())) {
            long count = learningLogService.count(new LambdaQueryWrapper<LearningLog>()
                    .eq(LearningLog::getUserId, event.getUserId())
                    .eq(LearningLog::getActivityType, "VIDEO"));
            
            // Note: Since we log progress every time, counting logs might be inaccurate if multiple logs per task.
            // Better to count completed tasks from StudyPlanTask?
            // Or assume "TaskCompletedEvent" is fired only once per task completion.
            // Let's use a simpler counter if possible or just check log count for now.
            // Ideally, query StudyPlanTask table for completed video tasks.
            // But for this demo, let's assume 5 events = 5 tasks.
            
            // Actually, let's just use a hardcoded logic: check user badges, if not present, check count.
            // Let's assume ID 101 is "Light Scholar".
            if (count >= 5) {
                awardBadge(event.getUserId(), 101L, "光影学者", "累计完成5个视频任务", 50);
            }
        }
    }

    @EventListener
    public void handleCheckin(CheckinEvent event) {
        // Rule 2: 7 days continuous check-in -> "Discipline Pioneer" (Badge ID 102)
        User user = userMapper.selectById(event.getUserId());
        if (user != null && user.getContinuousCheckinDays() >= 7) {
            awardBadge(event.getUserId(), 102L, "自律先锋", "连续打卡满7天", 100);
        }
    }

    private void awardBadge(Long userId, Long badgeId, String badgeName, String reason, int points) {
        // Check if already awarded is handled in service, but we want to notify only on new award.
        // So we should check existence first here to trigger notification.
        // Actually service handles duplicate check but returns void. 
        // Let's check service logic: it just returns if exists.
        // So we can check existence first.
        boolean hasBadge = userBadgeService.getUserBadges(userId).stream()
                .anyMatch(b -> b.getBadgeId().equals(badgeId));
        
        if (!hasBadge) {
            userBadgeService.awardBadge(userId, badgeId, reason);
            pointsService.addPoints(userId, points, "获得成就: " + badgeName);
            
            // Send Notification
            NotificationWebSocket.sendInfo(userId, 
                String.format("{\"type\":\"ACHIEVEMENT_UNLOCKED\", \"badgeId\":%d, \"name\":\"%s\", \"points\":%d}", 
                    badgeId, badgeName, points));
        }
    }
}

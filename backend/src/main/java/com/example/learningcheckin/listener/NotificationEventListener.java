package com.example.learningcheckin.listener;

import com.example.learningcheckin.event.CheckinEvent;
import com.example.learningcheckin.event.TaskGradedEvent;
import com.example.learningcheckin.service.INotificationService;
import com.example.learningcheckin.websocket.NotificationWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    @Autowired
    private INotificationService notificationService;

    @EventListener
    public void handleTaskGraded(TaskGradedEvent event) {
        String title = "作业已评分";
        String content = "您的作业已评分。分数: " + event.getScore();
        if (event.getEarnedPoints() > 0) {
            content += "，获得积分: " + event.getEarnedPoints();
        }

        // Save to DB
        notificationService.sendNotification(event.getUserId(), title, content, "TASK");

        // Send WebSocket
        NotificationWebSocket.sendInfo(event.getUserId(), 
            String.format("{\"type\":\"TASK_GRADED\", \"taskId\":%d, \"score\":%d, \"points\":%d}", 
                event.getTaskId(), event.getScore(), event.getEarnedPoints()));
    }

    @EventListener
    public void handleCheckin(CheckinEvent event) {
        if (!event.isMakeup()) {
            // Optional: Notify user of successful checkin + points
            // notificationService.sendNotification(event.getUserId(), "打卡成功", "今日打卡成功，获得10积分！", "SYSTEM");
        }
    }
}

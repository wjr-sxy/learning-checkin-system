package com.example.learningcheckin.listener;

import com.example.learningcheckin.event.CheckinEvent;
import com.example.learningcheckin.event.ProductExchangeEvent;
import com.example.learningcheckin.event.TaskGradedEvent;
import com.example.learningcheckin.service.IPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PointsEventListener {

    @Autowired
    private IPointsService pointsService;

    @EventListener
    public void handleTaskGraded(TaskGradedEvent event) {
        if (event.getEarnedPoints() != null && event.getEarnedPoints() > 0) {
            pointsService.addPoints(event.getUserId(), event.getEarnedPoints(), 
                "Task Graded (ID: " + event.getTaskId() + ")");
        }
    }

    @EventListener
    public void handleCheckin(CheckinEvent event) {
        if (!event.isMakeup()) {
            pointsService.addPoints(event.getUserId(), 10, "Daily Checkin: " + event.getDate());
        }
    }

    @EventListener
    public void handleProductExchange(ProductExchangeEvent event) {
        // Points deduction is handled in Service before event for consistency/validation.
        // But we could add "Cashback" or "Experience" logic here if needed.
        // For now, just logging or potential future expansion.
    }
}

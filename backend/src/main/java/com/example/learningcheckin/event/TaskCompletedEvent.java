package com.example.learningcheckin.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class TaskCompletedEvent extends ApplicationEvent {
    private final Long userId;
    private final Long taskId;
    private final String taskType;

    public TaskCompletedEvent(Object source, Long userId, Long taskId, String taskType) {
        super(source);
        this.userId = userId;
        this.taskId = taskId;
        this.taskType = taskType;
    }
}

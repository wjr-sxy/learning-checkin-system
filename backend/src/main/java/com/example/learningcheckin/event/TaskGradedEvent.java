package com.example.learningcheckin.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class TaskGradedEvent extends ApplicationEvent {
    private final Long userId;
    private final Long taskId;
    private final Long submissionId;
    private final Integer score;
    private final Integer earnedPoints;

    public TaskGradedEvent(Object source, Long userId, Long taskId, Long submissionId, Integer score, Integer earnedPoints) {
        super(source);
        this.userId = userId;
        this.taskId = taskId;
        this.submissionId = submissionId;
        this.score = score;
        this.earnedPoints = earnedPoints;
    }
}

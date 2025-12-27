package com.example.learningcheckin.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class CheckinEvent extends ApplicationEvent {
    private final Long userId;
    private final LocalDate date;
    private final Integer points;
    private final boolean isMakeup;

    public CheckinEvent(Object source, Long userId, LocalDate date, Integer points, boolean isMakeup) {
        super(source);
        this.userId = userId;
        this.date = date;
        this.points = points;
        this.isMakeup = isMakeup;
    }
}

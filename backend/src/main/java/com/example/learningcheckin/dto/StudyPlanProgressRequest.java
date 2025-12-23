package com.example.learningcheckin.dto;

import lombok.Data;

@Data
public class StudyPlanProgressRequest {
    private Integer completedTasks;
    private Integer totalTasks;
    private String note;
}

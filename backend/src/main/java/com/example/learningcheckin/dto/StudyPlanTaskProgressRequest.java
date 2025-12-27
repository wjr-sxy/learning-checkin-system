package com.example.learningcheckin.dto;

import lombok.Data;

@Data
public class StudyPlanTaskProgressRequest {
    private Integer progress; // Incremental value or new absolute value
    private Integer duration; // Seconds spent
    private Boolean isIncremental; // If true, add to current; if false, replace
}

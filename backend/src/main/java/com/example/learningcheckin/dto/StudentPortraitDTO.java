package com.example.learningcheckin.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StudentPortraitDTO {
    // Radar Chart Data (0-100)
    private Double completionScore;
    private Double punctualityScore;
    private Double qualityScore;
    private Double interactionScore;
    private Double growthScore;

    // Basic Stats
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer totalCheckins;
    private Double averageScore;
    
    // Heatmap Data (Date string -> count/status)
    private List<Map<String, Object>> activityHeatmap; // [{date: '2023-10-01', count: 1}]
}

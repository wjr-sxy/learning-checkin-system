package com.example.learningcheckin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StudentAbnormalDTO {
    private Long studentId;
    private String studentName;
    private String studentNumber; // Assuming User or Student has this, otherwise just Name
    private BigDecimal completionRate; // Task completion rate
    private Integer totalTasks;
    private Integer completedTasks;
}

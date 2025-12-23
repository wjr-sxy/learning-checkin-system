package com.example.learningcheckin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseStudentDTO {
    private Long id; // User ID
    private String username;
    private String email;
    private String studentId; // If exists
    private Integer points;
    private Integer status; // Course status: 0 normal, 1 banned
    private LocalDateTime joinTime;
}

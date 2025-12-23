package com.example.learningcheckin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DailyStatsDTO {
    private LocalDate date;
    private Integer totalStudents;
    private Integer checkedInCount;
    private BigDecimal checkinRate;
    private List<StudentAbnormalDTO> abnormalStudents;
}

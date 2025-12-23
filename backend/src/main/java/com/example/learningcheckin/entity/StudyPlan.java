package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_study_plan")
public class StudyPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long courseId;
    private String title;
    private String description;
    private Integer targetHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status; // 0: Ongoing, 1: Completed, 2: Expired
    private Integer totalTasks;
    private Integer completedTasks;
    private BigDecimal progressPercentage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isPointEligible;
}

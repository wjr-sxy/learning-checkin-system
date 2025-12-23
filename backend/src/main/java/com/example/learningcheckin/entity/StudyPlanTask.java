package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_study_plan_task")
public class StudyPlanTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private String title; // Task Title
    private String description; // Task Description
    private String standard; // Completion standard
    private Integer priority; // Priority
    private LocalDate deadline;
    private Integer status; // 0: Pending, 1: Completed
    private LocalDateTime createTime;
}

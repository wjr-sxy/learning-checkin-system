package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private Long courseId;
    private String title;
    private String content;
    private String attachmentUrl;
    private LocalDateTime deadline;
    private Integer rewardPoints;
    private String submitType; // TEXT, FILE, IMAGE
    private Integer status; // 0: Draft, 1: Published
    private Boolean isRecurring;
    private String frequency; // DAILY, WEEKLY
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer makeupCount;
    private Integer makeupCostPercent;
    private String contentTemplate;
    private String reminderConfig; // JSON: [24, 3, 1] (hours before deadline)
    private LocalDateTime createTime;
}

package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_task_checkin")
public class TaskCheckin {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long studentId;
    private LocalDate date; // The date this checkin belongs to
    private LocalDateTime checkinTime; // Actual operation time
    private String content;
    private String fileUrl;
    private Boolean isMakeup;
    private Integer pointsAwarded;
}

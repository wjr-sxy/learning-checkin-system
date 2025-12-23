package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_task_submission")
public class TaskSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long studentId;
    private String content;
    private String fileUrls; // JSON string
    private Integer status; // 0: Pending, 1: Graded, 2: Returned
    private Integer score;
    private Integer rating;
    private String comment;
    private LocalDateTime submitTime;
    private LocalDateTime gradeTime;
}

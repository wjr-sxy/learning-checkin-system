package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_learning_log")
public class LearningLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long taskId;
    private String activityType; // VIDEO_WATCH, QUIZ_PASS, etc.
    private Integer duration; // Seconds
    private String data; // JSON details
    private LocalDateTime createTime;
}

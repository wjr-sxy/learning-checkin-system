package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_task_submission_history")
public class TaskSubmissionHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private Integer score;
    private String comment;
    private Long graderId;
    private LocalDateTime operateTime;
    private Integer previousScore;
    private String previousComment;
}

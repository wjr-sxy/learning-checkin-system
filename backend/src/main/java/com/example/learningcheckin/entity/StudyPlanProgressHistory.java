package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_study_plan_progress_history")
public class StudyPlanProgressHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private BigDecimal previousProgress;
    private BigDecimal newProgress;
    private Integer completedTasks;
    private Integer totalTasks;
    private String note;
    private LocalDateTime createTime;
}

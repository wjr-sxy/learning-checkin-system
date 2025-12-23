package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_daily_online_stats")
public class DailyOnlineStats {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate statsDate;
    private Long durationSeconds;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String logType; // OPERATION, LOGIN, SENSITIVE, EXPIRATION
    private Long userId;
    private String module;
    private String action;
    private String content;
    private String ip;
    private Integer status; // 0: Success, 1: Fail
    private Long executionTime;
    private String extraInfo; // JSON string
    private LocalDateTime createTime;
}

package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_blacklist")
public class Blacklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type; // JWT, IP, SENSITIVE_WORD
    private String value;
    private String reason;
    private LocalDateTime createTime;
}

package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_notice")
public class SysNotice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type; // MESSAGE, NOTIFICATION, ANNOUNCEMENT
    private Long senderId; // 0 for System
    private Long receiverId; // NULL for Announcement
    private String title;
    private String content;
    private Integer isRead; // 0: No, 1: Yes
    private LocalDateTime createTime;
}

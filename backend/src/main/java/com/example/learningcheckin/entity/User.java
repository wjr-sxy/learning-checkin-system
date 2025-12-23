package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private Integer points;
    private String role;
    private Integer status; // 0: Normal, 1: Banned
    private Integer continuousCheckinDays;
    private java.time.LocalDate lastCheckinDate;
    
    // New fields for Personal Center
    private String fullName;
    private String college;
    private String phone;
    
    // Decorations
    private String currentAvatarFrame;
    private String currentSkin;
    private String currentBadge;
    private String profileBackground;

    // Online Stats
    private Long totalOnlineSeconds;
    private LocalDateTime lastActiveTime;
    
    // Privacy
    private Boolean allowFriendAdd;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

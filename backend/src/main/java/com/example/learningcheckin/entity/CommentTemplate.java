package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_comment_template")
public class CommentTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private String content;
    private String category;
    private Integer usageCount;
    private LocalDateTime createTime;
}

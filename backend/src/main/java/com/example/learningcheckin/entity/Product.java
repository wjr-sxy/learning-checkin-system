package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private String imageUrl;
    private Integer stock;
    private String type;
    private String category;
    private String subCategory;
    private LocalDateTime validUntil;
    private Integer status; // 0: Off-shelf, 1: On-shelf
    private LocalDateTime createTime;

    /**
     * Valid duration in days (null or 0 means permanent)
     */
    private Integer days;

    /**
     * Video URL for dynamic background or preview
     */
    private String videoUrl;

    /**
     * Expiration time for the current user (not persisted in product table)
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private LocalDateTime expireTime;
}

package com.example.learningcheckin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Integer price;
    private Integer status; // 0: Success, 1: Refunded
    private Boolean isAbnormal; // Abnormal Order
    
    // Shipping Info
    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;
    private String trackingNumber;
    private Integer shippingStatus; // 0: Pending, 1: Shipped, 2: Delivered
    
    private LocalDateTime createTime;
}

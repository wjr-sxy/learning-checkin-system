package com.example.learningcheckin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.Checkin;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.CheckinMapper;
import com.example.learningcheckin.mapper.OrderMapper;
import com.example.learningcheckin.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class StatsController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CheckinMapper checkinMapper;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total Users
        stats.put("totalUsers", userMapper.selectCount(null));
        
        // Today's Checkins
        stats.put("todayCheckins", checkinMapper.selectCount(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getCheckinDate, LocalDate.now())));
        
        // Total Orders
        stats.put("totalOrders", orderMapper.selectCount(null));
        
        // Today's Orders
        // Need to handle LocalDateTime for Order createTime
        // Simplified: just total orders for now, or can add complex query if needed.
        // Let's add "Users with Points > 0" as a proxy for active users
        stats.put("activeUsers", userMapper.selectCount(new LambdaQueryWrapper<User>().gt(User::getPoints, 0)));

        return Result.success(stats);
    }
}

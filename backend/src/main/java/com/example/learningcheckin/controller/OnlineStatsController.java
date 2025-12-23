package com.example.learningcheckin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.DailyOnlineStats;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.DailyOnlineStatsMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.websocket.NotificationWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/stats/online")
@CrossOrigin
public class OnlineStatsController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DailyOnlineStatsMapper dailyOnlineStatsMapper;
    
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    
    // Simple in-memory peak storage (resets on restart)
    private static AtomicInteger peakOnline = new AtomicInteger(0);

    @PostMapping("/heartbeat")
    public Result<Void> heartbeat(@RequestBody Map<String, Object> body) {
        try {
            Object userIdObj = body.get("userId");
            Object durationObj = body.get("duration");

            if (userIdObj == null || durationObj == null) {
                return Result.error(400, "Invalid parameters");
            }

            Long userId = Long.valueOf(userIdObj.toString());
            Long duration = Long.valueOf(durationObj.toString());

            // Update User total time and last active
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setTotalOnlineSeconds((user.getTotalOnlineSeconds() == null ? 0 : user.getTotalOnlineSeconds()) + duration);
                user.setLastActiveTime(LocalDateTime.now(ZONE_ID));
                userMapper.updateById(user);
            }

            // Update Daily Stats
            dailyOnlineStatsMapper.incrementDuration(userId, LocalDate.now(ZONE_ID), duration);

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/user")
    public Result<Map<String, Object>> getUserStats(@RequestParam Long userId) {
        Map<String, Object> data = new HashMap<>();
        
        User user = userMapper.selectById(userId);
        if (user != null) {
            data.put("totalSeconds", user.getTotalOnlineSeconds() == null ? 0 : user.getTotalOnlineSeconds());
        } else {
            data.put("totalSeconds", 0);
        }

        QueryWrapper<DailyOnlineStats> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("stats_date", LocalDate.now(ZONE_ID));
        DailyOnlineStats daily = dailyOnlineStatsMapper.selectOne(qw);
        data.put("todaySeconds", daily != null ? daily.getDurationSeconds() : 0);

        return Result.success(data);
    }

    @GetMapping("/leaderboard")
    public Result<List<Map<String, Object>>> getLeaderboard(
            @RequestParam(defaultValue = "month") String type) { // week, month
        
        LocalDate startDate = null;
        if ("week".equalsIgnoreCase(type)) {
            startDate = LocalDate.now(ZONE_ID).minusWeeks(1);
        } else if ("month".equalsIgnoreCase(type)) {
            startDate = LocalDate.now(ZONE_ID).minusMonths(1);
        }

        List<Map<String, Object>> list;
        if (startDate != null) {
            list = dailyOnlineStatsMapper.getTeacherStatsByDateRange(startDate);
        } else {
            // Fallback to total accumulated time in sys_user
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.eq("role", "TEACHER");
            qw.orderByDesc("total_online_seconds");
            qw.last("LIMIT 100");
            qw.select("id", "username", "full_name", "avatar", "total_online_seconds as total_seconds", "college");
            list = userMapper.selectMaps(qw);
        }
        
        return Result.success(list);
    }

    @GetMapping("/platform")
    public Result<Map<String, Object>> getPlatformStats() {
        try {
            Map<String, Object> data = new HashMap<>();
            
            // Current Online: Active in last 5 minutes
            LocalDateTime fiveMinAgo = LocalDateTime.now(ZONE_ID).minusMinutes(5);
            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.gt("last_active_time", fiveMinAgo);
            Long currentOnline = userMapper.selectCount(qw);
            
            // Update Peak
            if (currentOnline > peakOnline.get()) {
                peakOnline.set(currentOnline.intValue());
            }
            
            data.put("currentOnline", currentOnline);
            data.put("peakOnline", peakOnline.get());
            
            // Average Online Time (Total Time / Total Users)
            Long totalTime = userMapper.sumTotalOnlineTime();
            Long totalUsers = userMapper.selectCount(null); // Count all users
            
            double avgTime = (totalUsers != null && totalUsers > 0 && totalTime != null) 
                             ? (double) totalTime / totalUsers 
                             : 0.0;
            
            data.put("avgOnlineTime", avgTime);

            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "Failed to get platform stats: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getOnlineUsersList() {
        try {
            java.util.Set<Long> onlineUserIds = NotificationWebSocket.getOnlineUserIds();
            if (onlineUserIds == null || onlineUserIds.isEmpty()) {
                return Result.success(java.util.Collections.emptyList());
            }

            QueryWrapper<User> qw = new QueryWrapper<>();
            qw.in("id", onlineUserIds);
            qw.select("id", "username", "full_name", "avatar", "role", "last_active_time");
            
            List<Map<String, Object>> list = userMapper.selectMaps(qw);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "获取在线用户列表失败: " + e.getMessage());
        }
    }
}

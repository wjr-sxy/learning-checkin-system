package com.example.learningcheckin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.PointsRecord;
import com.example.learningcheckin.entity.SysConfig;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.PointsRecordMapper;
import com.example.learningcheckin.mapper.SysConfigMapper;
import com.example.learningcheckin.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/points")
public class AdminPointsController {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getPointsStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Calculate Today's Issuance
        List<PointsRecord> todayRecords = pointsRecordMapper.selectList(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getType, 1)
                .ge(PointsRecord::getCreateTime, LocalDate.now().atStartOfDay()));
        int todayIssuance = todayRecords.stream().mapToInt(PointsRecord::getAmount).sum();

        // 2. Calculate Current Total Circulation (Sum of all user points)
        List<User> users = userMapper.selectList(null);
        long totalCirculation = users.stream()
                .filter(u -> u.getPoints() != null)
                .mapToLong(User::getPoints)
                .sum();

        // 3. Calculate Inflation Rate (Week over Week)
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<PointsRecord> weekRecords = pointsRecordMapper.selectList(new LambdaQueryWrapper<PointsRecord>()
                .ge(PointsRecord::getCreateTime, oneWeekAgo));

        long weekIssuance = weekRecords.stream()
                .filter(r -> r.getType() == 1)
                .mapToLong(PointsRecord::getAmount)
                .sum();

        long weekConsumption = weekRecords.stream()
                .filter(r -> r.getType() == 2)
                .mapToLong(PointsRecord::getAmount)
                .sum();

        long netChange = weekIssuance - weekConsumption;
        long startOfWeekCirculation = totalCirculation - netChange;

        double inflationRate = 0.0;
        if (startOfWeekCirculation > 0) {
            inflationRate = (double) netChange / startOfWeekCirculation * 100;
        }

        boolean inflationWarning = inflationRate > 15.0;
        String inflationAdvice = inflationWarning ? "警告：通胀率过高！建议降低基础积分获取或提高商品价格。" : "正常";

        stats.put("todayIssuance", todayIssuance);
        stats.put("totalCirculation", totalCirculation);
        stats.put("inflationRate", String.format("%.2f", inflationRate));
        stats.put("inflationWarning", inflationWarning);
        stats.put("inflationAdvice", inflationAdvice);

        return Result.success(stats);
    }

    @GetMapping("/rules")
    public Result<List<SysConfig>> getRules() {
        // Return all config items starting with 'points_rule' or the multiplier
        List<SysConfig> allConfigs = sysConfigMapper.selectList(null);
        List<SysConfig> pointRules = allConfigs.stream()
                .filter(c -> c.getConfigKey().startsWith("points_rule") || c.getConfigKey().equals("points_multiplier"))
                .collect(Collectors.toList());
        return Result.success(pointRules);
    }

    @PostMapping("/rules")
    public Result<String> updateRule(@RequestBody SysConfig rule) {
        if (rule.getId() != null) {
            sysConfigMapper.updateById(rule);
        } else {
            sysConfigMapper.insert(rule);
        }
        return Result.success("Rule updated successfully");
    }
}

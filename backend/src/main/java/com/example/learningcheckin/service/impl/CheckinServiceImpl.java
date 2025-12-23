package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.entity.Checkin;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.CheckinMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.service.ICheckinService;
import com.example.learningcheckin.service.IPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CheckinServiceImpl extends ServiceImpl<CheckinMapper, Checkin> implements ICheckinService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IPointsService pointsService;

    @Override
    public boolean isCheckedIn(Long userId, LocalDate date) {
        return this.count(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getUserId, userId)
                .eq(Checkin::getCheckinDate, date)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Checkin dailyCheckin(Long userId) {
        LocalDate today = LocalDate.now();
        if (isCheckedIn(userId, today)) {
            throw new RuntimeException("今日已打卡");
        }

        // 1. Create Checkin Record
        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkin.setCheckinTime(LocalDateTime.now());
        checkin.setStudyDuration(0); 
        checkin.setIsSupplementary(false);
        this.save(checkin);

        // 2. Update Continuous Days and Last Check-in Date
        updateContinuousDays(userId);

        // 3. Add Points (10 points)
        pointsService.addPoints(userId, 10, "每日打卡");

        return checkin;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Checkin reCheckin(Long userId, LocalDate date) {
        LocalDate today = LocalDate.now();
        if (!date.isBefore(today)) {
            throw new RuntimeException("只能补签过去的日期");
        }
        
        if (isCheckedIn(userId, date)) {
            throw new RuntimeException("该日期已打卡");
        }

        // Check monthly limit (2 times)
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        
        Long count = this.count(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getUserId, userId)
                .eq(Checkin::getIsSupplementary, true)
                .between(Checkin::getCheckinDate, firstDayOfMonth, lastDayOfMonth));
        
        if (count >= 2) {
            throw new RuntimeException("本月补卡次数已用完");
        }

        // Deduct 20 points
        pointsService.deductPoints(userId, 20, "补卡: " + date.toString());

        // Create Checkin
        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(date);
        checkin.setCheckinTime(LocalDateTime.now());
        checkin.setStudyDuration(0);
        checkin.setIsSupplementary(true);
        this.save(checkin);

        // Recalculate continuous days
        updateContinuousDays(userId);

        return checkin;
    }

    @Override
    public List<Checkin> getHistory(Long userId) {
        return this.list(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getUserId, userId)
                .orderByDesc(Checkin::getCheckinDate));
    }

    private void updateContinuousDays(Long userId) {
        User user = userMapper.selectById(userId);
        // Get all check-in dates
        List<Checkin> list = this.list(new LambdaQueryWrapper<Checkin>()
                .eq(Checkin::getUserId, userId)
                .select(Checkin::getCheckinDate)
                .orderByDesc(Checkin::getCheckinDate));
        
        if (list.isEmpty()) {
            user.setContinuousCheckinDays(0);
            userMapper.updateById(user);
            return;
        }

        Set<LocalDate> dates = list.stream().map(Checkin::getCheckinDate).collect(Collectors.toSet());
        LocalDate current = LocalDate.now();
        
        // If not checked in today, check if checked in yesterday. 
        // If neither, streak is broken (0).
        // Exception: If calculating AFTER a re-checkin in the past, the streak might be preserved relative to THAT day?
        // No, "Continuous Check-in Days" usually means "Streak ending Today (or Yesterday)".
        
        int streak = 0;
        if (dates.contains(current)) {
            streak = 1;
            LocalDate d = current.minusDays(1);
            while (dates.contains(d)) {
                streak++;
                d = d.minusDays(1);
            }
        } else if (dates.contains(current.minusDays(1))) {
            // If checked in yesterday but not today, streak is still valid (waiting for today)
            streak = 1;
            LocalDate d = current.minusDays(2);
            while (dates.contains(d)) {
                streak++;
                d = d.minusDays(1);
            }
        } else {
            streak = 0;
        }

        user.setContinuousCheckinDays(streak);
        // Update last checkin date to the latest one in DB
        user.setLastCheckinDate(list.get(0).getCheckinDate());
        userMapper.updateById(user);
    }
}

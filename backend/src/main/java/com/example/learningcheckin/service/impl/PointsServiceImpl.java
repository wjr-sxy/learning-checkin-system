package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.dto.TrendPointDTO;
import com.example.learningcheckin.entity.PointsRecord;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.PointsRecordMapper;
import com.example.learningcheckin.mapper.UserMapper;
import com.example.learningcheckin.service.IPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PointsServiceImpl extends ServiceImpl<PointsRecordMapper, PointsRecord> implements IPointsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, Integer amount, String description) {
        if (amount <= 0) return;

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user.setPoints(user.getPoints() + amount);
        userMapper.updateById(user);

        PointsRecord pr = new PointsRecord();
        pr.setUserId(userId);
        pr.setType(1); // Gain
        pr.setAmount(amount);
        pr.setDescription(description);
        pr.setCreateTime(LocalDateTime.now());
        this.save(pr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long userId, Integer amount, String description) {
        if (amount <= 0) return;

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.getPoints() < amount) {
            throw new RuntimeException("积分不足");
        }

        user.setPoints(user.getPoints() - amount);
        userMapper.updateById(user);

        PointsRecord pr = new PointsRecord();
        pr.setUserId(userId);
        pr.setType(2); // Consume
        pr.setAmount(amount);
        pr.setDescription(description);
        pr.setCreateTime(LocalDateTime.now());
        this.save(pr);
    }

    @Override
    public List<PointsRecord> getPointsLog(Long userId) {
        return this.list(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, userId)
                .orderByDesc(PointsRecord::getCreateTime));
    }

    @Override
    public List<TrendPointDTO> getPointsTrend(Long userId, Integer days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<PointsRecord> records = this.list(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getUserId, userId)
                .ge(PointsRecord::getCreateTime, startDate)
                .orderByAsc(PointsRecord::getCreateTime));

        // Group by Date
        // Map<LocalDate, Integer> dailyChange = records.stream()
        //         .collect(Collectors.groupingBy(
        //                 r -> r.getCreateTime().toLocalDate(),
        //                 Collectors.summingInt(r -> r.getType() == 1 ? r.getAmount() : -r.getAmount())
        //         ));

        List<TrendPointDTO> result = new ArrayList<>();
        LocalDate current = LocalDate.now().minusDays(days - 1);
        LocalDate end = LocalDate.now();

        // To calculate cumulative, we need initial points at start date?
        // Requirement says "Points Variation Trend" (积分变化折线图). 
        // Usually this means total balance over time OR daily gain/loss.
        // Assuming "Balance Trend" is better, but without snapshot history, it's hard to be exact.
        // Let's show "Daily Net Change" or "Balance Simulation".
        // For simplicity and accuracy with current data model, let's show "Daily Net Change" (Points earned/spent per day).
        // Or if we want balance, we assume current balance and work backwards?
        // Let's implement "Balance" by working backwards from current points.
        
        User user = userMapper.selectById(userId);
        int currentPoints = user != null ? user.getPoints() : 0;
        
        // Get all records after startDate to now to back-calculate
        // We already have them in `records`.
        // But we need records strictly after "current date iteration" to adjust balance?
        // Actually, working forward is easier if we know start balance.
        // Working backward:
        // Balance(Today) = CurrentPoints
        // Balance(Yesterday) = Balance(Today) - NetChange(Today)
        
        // Let's map all records to dates first
        Map<LocalDate, Integer> dailyNet = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCreateTime().toLocalDate(),
                        Collectors.summingInt(r -> r.getType() == 1 ? r.getAmount() : -r.getAmount())
                ));

        // Fill result working backwards
        int runningBalance = currentPoints;
        // We need to go from Today back to StartDate
        for (LocalDate date = end; !date.isBefore(current); date = date.minusDays(1)) {
            TrendPointDTO dto = new TrendPointDTO();
            dto.setDate(date.toString());
            dto.setValue(runningBalance); // Balance at end of day
            result.add(0, dto); // Add to front

            // For previous day, subtract today's net change
            int netChange = dailyNet.getOrDefault(date, 0);
            runningBalance -= netChange;
        }
        
        return result;
    }
}

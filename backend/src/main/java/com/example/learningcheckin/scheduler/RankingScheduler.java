package com.example.learningcheckin.scheduler;

import com.example.learningcheckin.dto.RankingDTO;
import com.example.learningcheckin.entity.PointsRecord;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.PointsRecordMapper;
import com.example.learningcheckin.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class RankingScheduler {

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private UserMapper userMapper;

    // Run at 00:00:00 every day
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void settleDailyRanking() {
        System.out.println("Daily Ranking Calculation - " + LocalDateTime.now());
        
        // Calculate for Yesterday
        LocalDateTime startOfYesterday = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
        List<RankingDTO> list = pointsRecordMapper.selectRanking(startOfYesterday, 3); // Top 3

        for (int i = 0; i < list.size(); i++) {
            RankingDTO dto = list.get(i);
            int rewardPoints = 0;
            String rankDesc = "";
            
            if (i == 0) { rewardPoints = 50; rankDesc = "Daily 1st"; }
            else if (i == 1) { rewardPoints = 30; rankDesc = "Daily 2nd"; }
            else if (i == 2) { rewardPoints = 10; rankDesc = "Daily 3rd"; }
            
            if (rewardPoints > 0) {
                giveReward(dto.getUserId(), rewardPoints, rankDesc);
            }
        }
    }
    
    // Run at 00:00:00 every Monday
    @Scheduled(cron = "0 0 0 ? * MON")
    @Transactional(rollbackFor = Exception.class)
    public void settleWeeklyRanking() {
        System.out.println("Weekly Ranking Calculation - " + LocalDateTime.now());
        
        // Calculate for Last Week
        // Today is Monday. Last week started 7 days ago.
        LocalDateTime startOfLastWeek = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        List<RankingDTO> list = pointsRecordMapper.selectRanking(startOfLastWeek, 3); // Top 3

        for (int i = 0; i < list.size(); i++) {
            RankingDTO dto = list.get(i);
            int rewardPoints = 0;
            String rankDesc = "";
            
            if (i == 0) { rewardPoints = 200; rankDesc = "Weekly 1st"; }
            else if (i == 1) { rewardPoints = 100; rankDesc = "Weekly 2nd"; }
            else if (i == 2) { rewardPoints = 50; rankDesc = "Weekly 3rd"; }
            
            if (rewardPoints > 0) {
                giveReward(dto.getUserId(), rewardPoints, rankDesc);
            }
        }
    }

    private void giveReward(Long userId, int points, String reason) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPoints(user.getPoints() + points);
            userMapper.updateById(user);
            
            PointsRecord pr = new PointsRecord();
            pr.setUserId(userId);
            pr.setType(1); // Gain
            pr.setAmount(points);
            pr.setDescription("Ranking Reward: " + reason);
            pr.setCreateTime(LocalDateTime.now());
            pointsRecordMapper.insert(pr);
            
            System.out.println("Awarded " + points + " points to user " + userId + " for " + reason);
        }
    }
}

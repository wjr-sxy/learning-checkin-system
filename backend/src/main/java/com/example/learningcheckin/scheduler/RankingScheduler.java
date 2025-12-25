package com.example.learningcheckin.scheduler;

import com.example.learningcheckin.dto.RankingDTO;
import com.example.learningcheckin.entity.PointsRecord;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.PointsRecordMapper;
import com.example.learningcheckin.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
public class RankingScheduler {

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_DAILY_RANKING = "ranking:daily";
    private static final String REDIS_KEY_WEEKLY_RANKING = "ranking:weekly";

    // Run every 5 minutes to sync Redis
    @Scheduled(fixedRate = 300000)
    public void syncRankingToRedis() {
        // Daily Ranking
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        List<RankingDTO> dailyList = pointsRecordMapper.selectRanking(startOfDay, 100);
        
        // Clear old key? Or just add? ZAdd updates score if member exists.
        // To be clean, we might want to expire or delete, but simple ZADD is fine for continuous updates.
        // But if someone's score drops or they disappear (unlikely), they might remain.
        // For accurate "Top 100", we should probably replace the key content.
        // Strategy: Delete and Re-add is safest for consistency, but not atomic.
        // Better: Add all, then remove range outside 0-99.
        
        redisTemplate.delete(REDIS_KEY_DAILY_RANKING);
        for (RankingDTO dto : dailyList) {
            // ZSet stores score as double. We use score as score. Member is userId.
            redisTemplate.opsForZSet().add(REDIS_KEY_DAILY_RANKING, dto.getUserId().toString(), dto.getScore());
            // Store extra info (username, avatar) in a Hash or just cache user info separately?
            // Controller will need to fetch User info. 
            // Optimization: Store "userId:username:avatar" as member? No, ID is better for uniqueness.
            // We'll fetch user details in Controller from DB/Cache based on IDs from ZSet.
        }
        // Set Expiration to avoid stale data if scheduler stops? 1 hour?
        // redisTemplate.expire(REDIS_KEY_DAILY_RANKING, 1, TimeUnit.HOURS);

        // Weekly Ranking
        LocalDateTime startOfWeek = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), LocalTime.MIN);
        List<RankingDTO> weeklyList = pointsRecordMapper.selectRanking(startOfWeek, 100);
        
        redisTemplate.delete(REDIS_KEY_WEEKLY_RANKING);
        for (RankingDTO dto : weeklyList) {
            redisTemplate.opsForZSet().add(REDIS_KEY_WEEKLY_RANKING, dto.getUserId().toString(), dto.getScore());
        }
    }

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

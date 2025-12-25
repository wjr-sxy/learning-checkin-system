package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.dto.RankingDTO;
import com.example.learningcheckin.entity.User;
import com.example.learningcheckin.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_DAILY_RANKING = "ranking:daily";
    private static final String REDIS_KEY_WEEKLY_RANKING = "ranking:weekly";

    @GetMapping("/daily")
    public Result<List<RankingDTO>> getDailyRanking() {
        // Fetch from Redis ZSet
        return Result.success(getRankingFromRedis(REDIS_KEY_DAILY_RANKING));
    }

    @GetMapping("/weekly")
    public Result<List<RankingDTO>> getWeeklyRanking() {
        // Fetch from Redis ZSet
        return Result.success(getRankingFromRedis(REDIS_KEY_WEEKLY_RANKING));
    }

    private List<RankingDTO> getRankingFromRedis(String key) {
        Set<ZSetOperations.TypedTuple<String>> range = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 99);
        List<RankingDTO> result = new ArrayList<>();

        if (range == null || range.isEmpty()) {
            return result;
        }

        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : range) {
            String userIdStr = tuple.getValue();
            Double score = tuple.getScore();
            
            if (userIdStr != null) {
                Long userId = Long.valueOf(userIdStr);
                // Fetch user info (ideally from cache too, but DB is fast enough for 100 IDs or use local cache)
                User user = userMapper.selectById(userId);
                
                if (user != null) {
                    RankingDTO dto = new RankingDTO();
                    dto.setUserId(userId);
                    dto.setUsername(user.getUsername());
                    dto.setAvatar(user.getAvatar());
                    dto.setScore(score != null ? score.intValue() : 0);
                    dto.setRank(rank++);
                    result.add(dto);
                }
            }
        }
        return result;
    }
}

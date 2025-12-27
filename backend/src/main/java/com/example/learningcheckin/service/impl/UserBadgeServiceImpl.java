package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.entity.UserBadge;
import com.example.learningcheckin.mapper.UserBadgeMapper;
import com.example.learningcheckin.service.IUserBadgeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserBadgeServiceImpl extends ServiceImpl<UserBadgeMapper, UserBadge> implements IUserBadgeService {

    @Override
    public void awardBadge(Long userId, Long badgeId, String reason) {
        // Check if already awarded
        Long count = this.count(new LambdaQueryWrapper<UserBadge>()
                .eq(UserBadge::getUserId, userId)
                .eq(UserBadge::getBadgeId, badgeId));
        
        if (count > 0) return; // Already has badge

        UserBadge badge = new UserBadge();
        badge.setUserId(userId);
        badge.setBadgeId(badgeId);
        badge.setReason(reason);
        badge.setObtainTime(LocalDateTime.now());
        this.save(badge);
    }

    @Override
    public List<UserBadge> getUserBadges(Long userId) {
        return this.list(new LambdaQueryWrapper<UserBadge>()
                .eq(UserBadge::getUserId, userId)
                .orderByDesc(UserBadge::getObtainTime));
    }
}

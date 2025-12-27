package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.entity.UserBadge;

import java.util.List;

public interface IUserBadgeService extends IService<UserBadge> {
    void awardBadge(Long userId, Long badgeId, String reason);
    List<UserBadge> getUserBadges(Long userId);
}

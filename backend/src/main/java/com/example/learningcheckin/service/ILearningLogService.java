package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.entity.LearningLog;

public interface ILearningLogService extends IService<LearningLog> {
    void logActivity(Long userId, Long taskId, String type, Integer duration, String data);
}

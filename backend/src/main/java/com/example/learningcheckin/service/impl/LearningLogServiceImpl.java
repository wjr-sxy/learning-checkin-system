package com.example.learningcheckin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.learningcheckin.entity.LearningLog;
import com.example.learningcheckin.mapper.LearningLogMapper;
import com.example.learningcheckin.service.ILearningLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LearningLogServiceImpl extends ServiceImpl<LearningLogMapper, LearningLog> implements ILearningLogService {

    @Override
    public void logActivity(Long userId, Long taskId, String type, Integer duration, String data) {
        LearningLog log = new LearningLog();
        log.setUserId(userId);
        log.setTaskId(taskId);
        log.setActivityType(type);
        log.setDuration(duration);
        log.setData(data);
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
    }
}

package com.example.learningcheckin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.learningcheckin.dto.TrendPointDTO;
import com.example.learningcheckin.entity.PointsRecord;

import java.util.List;

public interface IPointsService extends IService<PointsRecord> {
    void addPoints(Long userId, Integer amount, String description);
    void deductPoints(Long userId, Integer amount, String description);
    List<PointsRecord> getPointsLog(Long userId);
    List<TrendPointDTO> getPointsTrend(Long userId, Integer days);
}

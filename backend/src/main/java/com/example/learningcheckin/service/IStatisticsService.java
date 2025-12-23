package com.example.learningcheckin.service;

import com.example.learningcheckin.dto.DailyStatsDTO;
import com.example.learningcheckin.dto.StudentPortraitDTO;
import com.example.learningcheckin.dto.TrendPointDTO;

import java.time.LocalDate;
import java.util.List;

public interface IStatisticsService {
    DailyStatsDTO getDailyStats(Long courseId, LocalDate date);
    List<TrendPointDTO> getCompletionTrend(Long courseId, Integer days);
    StudentPortraitDTO getStudentPortrait(Long courseId, Long studentId);
    List<java.util.Map<String, Object>> getLeaderboard(Long courseId, String type, Integer limit);
}

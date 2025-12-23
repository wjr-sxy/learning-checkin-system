package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.dto.DailyStatsDTO;
import com.example.learningcheckin.dto.TrendPointDTO;
import com.example.learningcheckin.service.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.example.learningcheckin.dto.StudentPortraitDTO;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin
public class StatisticsController {

    @Autowired
    private IStatisticsService statisticsService;

    @GetMapping("/daily")
    public Result<DailyStatsDTO> getDailyStats(
            @RequestParam Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(statisticsService.getDailyStats(courseId, date));
    }

    @GetMapping("/trend")
    public Result<List<TrendPointDTO>> getCompletionTrend(
            @RequestParam Long courseId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        return Result.success(statisticsService.getCompletionTrend(courseId, days));
    }

    @GetMapping("/student-portrait")
    public Result<StudentPortraitDTO> getStudentPortrait(
            @RequestParam(required = false) Long courseId,
            @RequestParam Long studentId) {
        return Result.success(statisticsService.getStudentPortrait(courseId, studentId));
    }

    @GetMapping("/leaderboard")
    public Result<List<java.util.Map<String, Object>>> getLeaderboard(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "points") String type,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statisticsService.getLeaderboard(courseId, type, limit));
    }
}

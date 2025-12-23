package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.dto.RankingDTO;
import com.example.learningcheckin.mapper.PointsRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired
    private PointsRecordMapper pointsRecordMapper;

    @GetMapping("/daily")
    public Result<List<RankingDTO>> getDailyRanking() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        List<RankingDTO> list = pointsRecordMapper.selectRanking(startOfDay, 10);
        return Result.success(populateRank(list));
    }

    @GetMapping("/weekly")
    public Result<List<RankingDTO>> getWeeklyRanking() {
        LocalDateTime startOfWeek = LocalDateTime.of(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), LocalTime.MIN);
        List<RankingDTO> list = pointsRecordMapper.selectRanking(startOfWeek, 10);
        return Result.success(populateRank(list));
    }

    private List<RankingDTO> populateRank(List<RankingDTO> list) {
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }
        return list;
    }
}

package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.dto.TrendPointDTO;
import com.example.learningcheckin.entity.PointsRecord;
import com.example.learningcheckin.service.IPointsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@CrossOrigin
public class PointsController {

    @Autowired
    private IPointsService pointsService;

    @GetMapping("/log")
    public Result<List<PointsRecord>> getPointsLog(@RequestParam Long userId) {
        return Result.success(pointsService.getPointsLog(userId));
    }

    @GetMapping("/trend")
    public Result<List<TrendPointDTO>> getPointsTrend(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "7") Integer days) {
        return Result.success(pointsService.getPointsTrend(userId, days));
    }
}

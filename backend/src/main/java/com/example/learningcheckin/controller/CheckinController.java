package com.example.learningcheckin.controller;

import com.example.learningcheckin.common.Result;
import com.example.learningcheckin.entity.Checkin;
import com.example.learningcheckin.service.ICheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
@CrossOrigin
public class CheckinController {

    @Autowired
    private ICheckinService checkinService;

    @GetMapping("/status")
    public Result<Boolean> getCheckinStatus(@RequestParam Long userId) {
        return Result.success(checkinService.isCheckedIn(userId, LocalDate.now()));
    }

    @PostMapping("/daily")
    public Result<Checkin> dailyCheckin(@RequestBody Map<String, Long> data) {
        Long userId = data.get("userId");
        try {
            Checkin checkin = checkinService.dailyCheckin(userId);
            return Result.success(checkin);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/recheckin")
    public Result<Checkin> reCheckin(@RequestBody Map<String, Object> data) {
        Long userId = Long.valueOf(data.get("userId").toString());
        String dateStr = data.get("date").toString();
        LocalDate date = LocalDate.parse(dateStr);
        try {
            Checkin checkin = checkinService.reCheckin(userId, date);
            return Result.success(checkin);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result<List<Checkin>> getHistory(@RequestParam Long userId) {
        return Result.success(checkinService.getHistory(userId));
    }
}
